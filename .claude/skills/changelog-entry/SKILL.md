---
name: changelog-entry
description: >
  Adds this PR's entry to CHANGELOG.md on the PR's own branch, then
  commits and pushes it. Runs as a workflow step of work-next-issue,
  between task-agent (which opens the PR) and review-fix-loop (which
  then reviews the entry with the rest of the diff). Takes the same
  pr_url/repo/branch arguments as review-fix-loop and emits them again.
  Use when a PR needs its changelog line, or "add this PR to the
  changelog".
tools: Bash, Read, Edit
---

# Changelog Entry

`CHANGELOG.md` follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) with one
release per calendar month, named `mon-yy` (e.g. `sep-26`), newest first. Every PR adds
its own line, so the changelog never has to be rebuilt from git history afterwards.

**Arguments** (`key=value`, same contract as `review-fix-loop`):
- `pr_url=<url>` — full GitHub PR URL. Required.
- `repo=<owner/repo>` — repository. Required.
- `branch=<branch>` — branch the PR is on. Required.

## Step 1 — Get the clone

Reuse the `task-agent` clone, as `review-fix-loop` does:

```bash
OWNER=${REPO%%/*}; REPO_NAME=${REPO#*/}; PR_NUMBER=${PR_URL##*/}
WORKDIR="${TASK_AGENT_WORKDIR:-$(python3 -c 'import os,tempfile;print(os.path.join(tempfile.gettempdir(),"task-agent"))')}"
LOCAL_PATH="$WORKDIR/$REPO_NAME"
if [ -d "$LOCAL_PATH/.git" ]; then
  git -C "$LOCAL_PATH" fetch origin
  git -C "$LOCAL_PATH" checkout "$BRANCH"
  git -C "$LOCAL_PATH" reset --hard "origin/$BRANCH"
else
  mkdir -p "$WORKDIR"
  git clone "https://github.com/$REPO.git" "$LOCAL_PATH"
  git -C "$LOCAL_PATH" checkout "$BRANCH"
fi
```

## Step 2 — Skip if the PR already has its entry

```bash
git -C "$LOCAL_PATH" diff "origin/master...HEAD" -- CHANGELOG.md
```

If that diff already adds a line (task-agent may have written one), do not add a second
one: go to Step 6 with `added=false`.

## Step 3 — Learn what the PR changed

```bash
gh pr view "$PR_NUMBER" --repo "$REPO" --json title,body
git -C "$LOCAL_PATH" diff --stat "origin/master...HEAD"
```

Read the title and body, and the diff where they do not make the user-visible effect
clear.

## Step 4 — Write the entry

Open `LOCAL_PATH/CHANGELOG.md` and find the section of the current month
(`date +%b-%y | tr 'A-Z' 'a-z'`, e.g. `sep-26`). If it does not exist, add it above
the newest `## ` release, below the intro paragraphs.

Put the entry under the right heading of that section, creating the heading if
needed, in this order: `### Added`, `### Changed`, `### Fixed`, `### Removed`.
- **Added**: new behaviour a user or client can see (a rule, an endpoint, a UI control, a test suite).
- **Changed**: different behaviour of something that existed, dependency bumps, CI and build changes.
- **Fixed**: a bug that is now gone.
- **Removed**: something that is gone.

The entry is one bullet, prepended at the top of its heading, that:
- says what changed for a user of the tool or the API, not how the code was organized;
- names the logic, endpoint or UI control it affects, in backticks where it is code;
- ends with the PR number in parentheses, e.g. `(#185)`;
- wraps at 120 characters, continuation lines indented by two spaces, as the file does.

If the PR belongs to a plan step whose earlier parts already have a bullet in the same
month (e.g. 12.2 after 12.1), extend that bullet with a clause and the new PR number
instead of adding a second bullet about the same feature.

## Step 5 — Commit and push

```bash
git -C "$LOCAL_PATH" add CHANGELOG.md
git -C "$LOCAL_PATH" commit -m "Add the CHANGELOG entry"
git -C "$LOCAL_PATH" push origin "$BRANCH"
```

Only `CHANGELOG.md` is committed. There is nothing to verify locally: no build or test
reads it.

## Step 6 — Report and emit workflow output

Print the bullet that was added (or that the PR already had one), then pass the PR on
to the next step unchanged:

```bash
if [ -n "$WORKFLOW_OUTPUT" ]; then
  printf '{"pr_url":"%s","repo":"%s","branch":"%s","added":%s}' \
    "$PR_URL" "$REPO" "$BRANCH" "$ADDED" > "$WORKFLOW_OUTPUT"
fi
```

## Rules

- Touch no file but `CHANGELOG.md`, and never rewrite or reorder existing entries.
- Never force-push: this step only adds a commit on top of the PR branch.
- One PR, one bullet (or one clause added to an existing bullet).
