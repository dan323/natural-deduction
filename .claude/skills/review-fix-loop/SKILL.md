---
name: review-fix-loop
description: >
  Drop-in replacement for task-agent's copilot-review-fixer that does
  not depend on GitHub Copilot reviewing the PR. Runs an independent
  reviewer agent (the built-in /code-review skill) and an independent
  fixer agent against a PR in a loop — review, fix, re-review — until
  the reviewer reports no more findings or a round cap is hit. Same
  argument contract as copilot-review-fixer (pr_url, repo, branch) so
  it slots into the same workflow position. Use when the repo has no
  Copilot review enabled.
tools: Bash, Read, Edit, Glob, Grep, Agent
---

# Review-Fix Loop

Two independent agents alternate, each spawned fresh (no shared context) so the
fixer never just rubber-stamps its own review:

1. **CI gate** — wait for the PR's checks to reach a terminal state, then treat any
   failing check as a finding, same as a review comment. `/code-review` only reads
   the diff — it does not run the test suite or CI, so a broken build never shows
   up as a review finding unless this step surfaces it explicitly.
2. **Reviewer** — runs `/code-review` (the built-in skill) against the PR's diff
   and posts findings as inline PR comments. It does not touch code.
3. **Fixer** — reads the reviewer's unresolved comments *and* any CI-failure
   comment this loop posted, and applies fixes, commits, pushes. It does not review.

Repeat until CI is green *and* the reviewer finds nothing, or `max_rounds` is
reached (default 3) — whichever comes first. A round that only fixes a CI failure
still counts against the cap.

**Arguments** (`key=value`, matches `copilot-review-fixer`'s contract):
- `pr_url=<url>` — full GitHub PR URL. Required.
- `repo=<owner/repo>` — repository. Required.
- `branch=<branch>` — branch the PR is on. Required.
- `max_rounds=<n>` — default `3`.
- `level=<low|medium|high>` — code-review effort. Default `medium`.

## Step 1 — Parse arguments and derive locals

```
OWNER, REPO_NAME  <- split repo on "/"
PR_NUMBER         <- last path segment of pr_url
```

Reuse the `task-agent` workdir convention so the clone is shared with a preceding
`task-agent` step in the same workflow:

```bash
WORKDIR="${TASK_AGENT_WORKDIR:-$(python3 -c 'import os,tempfile;print(os.path.join(tempfile.gettempdir(),"task-agent"))')}"
LOCAL_PATH="$WORKDIR/$REPO_NAME"
if [ -d "$LOCAL_PATH/.git" ]; then
  git -C "$LOCAL_PATH" fetch origin
  git -C "$LOCAL_PATH" checkout "$BRANCH"
  git -C "$LOCAL_PATH" reset --hard "origin/$BRANCH"
else
  mkdir -p "$WORKDIR"
  git clone "https://github.com/$OWNER/$REPO_NAME.git" "$LOCAL_PATH"
  git -C "$LOCAL_PATH" checkout "$BRANCH"
fi
```

## Step 2 — Loop

For `round = 1..max_rounds`:

### 2a. Wait for CI and gate on it

Poll until every check leaves `pending`/`queued`/`in_progress` (30s interval, ~10
minute cap):

```bash
until gh pr checks "$PR_NUMBER" --repo "$OWNER/$REPO_NAME" 2>&1 | grep -qv "pending\|queued\|in_progress" ; do
  sleep 30
done
gh pr checks "$PR_NUMBER" --repo "$OWNER/$REPO_NAME"
```

(`gh pr checks` exits non-zero when any check failed — that's expected here, don't
treat a non-zero exit alone as a script failure.) For each check whose status is
`fail`: fetch the failing job's log and post it as a PR issue comment so the fixer
picks it up the same way it picks up review comments:

```bash
RUN_ID=$(gh pr checks "$PR_NUMBER" --repo "$OWNER/$REPO_NAME" --json name,link,bucket \
  --jq '.[] | select(.bucket=="fail") | .link' | grep -oE '[0-9]+' | head -1)
LOG=$(gh run view "$RUN_ID" --repo "$OWNER/$REPO_NAME" --log-failed | tail -c 4000)
gh pr comment "$PR_NUMBER" --repo "$OWNER/$REPO_NAME" --body "CI failure — <check name>:
\`\`\`
$LOG
\`\`\`"
```

Track whether this round posted a CI-failure comment (`ci_failed=true/false`) — Step
2d's clean-pass check needs it alongside the review comment count.

If every check is green (or there are no checks configured), `ci_failed=false` and
continue to 2b.

### 2b. Spawn the reviewer (fresh agent, no memory of prior rounds)

```
Agent(
  subagent_type: "general-purpose",
  description: "Independent PR review, round <round>",
  prompt: """
    Repository: OWNER/REPO_NAME, PR #PR_NUMBER (branch BRANCH), local clone at LOCAL_PATH.

    Invoke the Skill tool with skill="code-review" and args="<level> <PR_NUMBER> --comment"
    against this PR. Do not read or fix code yourself beyond what the skill does —
    you are the reviewer, not the fixer. Report back only: how many findings were
    posted, and their one-line summaries.
  """
)
```

### 2c. Check for unresolved findings

```bash
gh api repos/$OWNER/$REPO_NAME/pulls/$PR_NUMBER/comments --paginate \
  --jq '[.[] | select(.in_reply_to_id == null)] | length'
```

If the reviewer posted zero new review comments this round (compare against the
count before this round started) **and** `ci_failed=false` from step 2a, stop the
loop — clean pass. Report success. Otherwise continue to 2d even if only one of
the two is true — a green review with red CI (or vice versa) is not done.

### 2d. Spawn the fixer (fresh agent, independent of the reviewer)

```
Agent(
  subagent_type: "general-purpose",
  description: "Independent PR fix, round <round>",
  prompt: """
    Repository: OWNER/REPO_NAME, PR #PR_NUMBER, branch BRANCH, local clone LOCAL_PATH.

    Read the PR's unresolved review comments (gh api repos/OWNER/REPO_NAME/pulls/PR_NUMBER/comments,
    or gh pr view PR_NUMBER --comments) that were posted by the review step just run,
    plus any "CI failure — <check>" issue comment posted this round (gh pr view
    PR_NUMBER --comments) — that's the only feedback in scope. Treat a CI failure as
    highest priority: read its log excerpt, find the root cause (it may be in a file
    the PR didn't touch, e.g. a pre-existing test whose fixture the PR's change now
    affects), and fix it first. For each actionable item: read the referenced file
    from LOCAL_PATH first, then apply the fix. Skip comments that are informational
    only or out of the PR's scope, and say why.

    Then:
      git -C LOCAL_PATH add -A
      git -C LOCAL_PATH diff --cached --stat   # confirm there is something to commit
      git -C LOCAL_PATH commit -m "Address review findings (round <round>)"
      git -C LOCAL_PATH push origin BRANCH --force-with-lease

    If nothing was fixable, say so explicitly instead of committing an empty change.
    Report a short summary: fixed (file:line — what), skipped (file:line — why).
  """
)
```

If the fixer made no commit (nothing actionable), stop the loop — further rounds
would just re-review the same unaddressed comments.

## Step 3 — Report

```
## Review-Fix Loop — Done

PR: <pr_url>
Rounds run: <n> / <max_rounds>
Outcome: <clean pass | fixer made no changes | round cap reached>
```

## Step 4 — Emit workflow output

```bash
if [ -n "$WORKFLOW_OUTPUT" ]; then
  printf '{"pr_url":"%s","repo":"%s/%s","branch":"%s","rounds":%d}' \
    "$PR_URL" "$OWNER" "$REPO_NAME" "$BRANCH" "$ROUNDS" > "$WORKFLOW_OUTPUT"
fi
```

## Rules

- Never call this loop's job done on review comments alone — always gate on CI too
  (step 2a). `/code-review` reads a diff; it has no way to know that change broke a
  test elsewhere in the repo. That gap is exactly why PR #134 (issue #133) got a
  clean review while its `test front` job was failing: the reviewer had nothing to
  say about it, and copilot-review-fixer never checked CI either, so nothing did.
- The reviewer must never edit files or run `git commit`/`git push` — that keeps it
  independent of the fixer, mirroring why Copilot's review and a human's fix used
  to be separate actors.
- The fixer must never invoke `/code-review` itself or grade its own work — it only
  reacts to comments already on the PR.
- Cap rounds so a disagreement between the two agents cannot loop forever; a round
  cap hit is not a failure, just report it and stop.
