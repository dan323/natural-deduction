---
name: sync-issues
description: >
  Reconciles this repo's open GitHub issues into a task-agent unified
  tasks.yml, without requiring a GitHub Projects board. New open
  issues (optionally filtered by label) land as status: pending tasks
  carrying the issue number as passthrough metadata; issues that were
  closed are marked skipped; tasks task-agent marked done get a
  comment posted on their issue linking the PR. Use as a workflow
  step in place of gh-project-sync when the repo has no project
  board — called by work-next-issue.
tools: Bash, Read, Write, Edit
---

# Sync Issues

Board-free stand-in for `gh-project-sync`: this repo (`dan323/natural-deduction`)
has no GitHub Projects (v2) board, so the task queue is just its open issues.
This skill keeps a unified `tasks.yml` (task-agent's unified format — full schema
in the `task-agent` plugin's `references/format.md`, findable via
`find ~/.claude/plugins -path '*task-agent/references/format.md'`) in sync with
`gh issue list`.

**Arguments** (`key=value`):
- `tasks=<path>` — path to the unified tasks.yml. Default: `tasks.yml` in repo root.
- `repo=<owner/repo>` — default: `dan323/natural-deduction`.
- `label=<label>` — only sync issues carrying this label. Default: unset (all open issues).

## Step 1 — Read current tasks.yml

Read `tasks=<path>` if it exists. If missing, start from:

```yaml
projects:
  - repo: <repo>
    tasks: []
```

Build a map of existing tasks keyed by `issue_number` (a passthrough field this
skill owns — never clobber other passthrough keys like `labels`/`priority` a human
added by hand).

## Step 2 — List open issues

```bash
gh issue list --repo "$REPO" --state open ${LABEL:+--label "$LABEL"} \
  --json number,title,url --limit 100
```

## Step 3 — Reconcile

For each open issue not yet in `tasks.yml`: append a new task, with an **explicit**
`id` — never `null` and never left for task-agent to synthesize later. `load`
computes the synthesized id only in the JSON it returns in memory; it never writes
that id back into the YAML file. If the file itself still says `id: null`, a later
`record` call re-reads the raw file, compares the raw (still-null) id against the
task-id it was given, and fails to match — the task can never be marked done/failed
in place. So this skill must write the same id `record` will later expect, using
task-agent's own algorithm:

```
id = md5(f"{repo}\n{description}")[:6]     # e.g. md5("dan323/natural-deduction\n#132: PR 6.1: ...")
```

```yaml
- id: <computed 6-hex-char id>
  description: "#<number>: <title>"
  status: pending
  issue_number: <number>
  issue_url: <url>
```

For each existing task whose `issue_number` is **no longer** in the open-issues list
(closed or deleted) and whose `status` is still `pending`: set `status: skipped`,
`reason: "issue closed"`, `date: <today, ISO>`. Never touch a task already `done`,
`failed`, or previously `skipped`.

For each existing task with `status: done` and a `pr_url` that has **not** yet been
announced (track this with a passthrough flag `pr_announced: true` once posted):
post a comment on its issue linking the PR, then set `pr_announced: true`.

```bash
gh issue comment "$ISSUE_NUMBER" --repo "$REPO" --body "Opened $PR_URL"
```

This mirrors `gh-project-sync`'s "move card to In Review" step, but since there's no
board, a comment is the only signal. Never close the issue here — closing is a human
call once the PR merges (same rule gh-project-sync follows for Done).

## Step 4 — Write tasks.yml

Write the file back with standard YAML (2-space indent, matching the example in
Step 3). Preserve every field already on each task entry — only add/modify the
fields this skill owns (`status`, `issue_number`, `issue_url`, `pr_announced`,
`reason`, `date` when this step sets them).

## Step 5 — Report and emit workflow output

Print a short summary: how many issues added as pending, how many marked skipped,
how many PR links announced.

```bash
if [ -n "$WORKFLOW_OUTPUT" ]; then
  printf '{"tasks":"%s","added":%d,"skipped":%d,"announced":%d}' \
    "$TASKS_PATH" "$ADDED" "$SKIPPED" "$ANNOUNCED" > "$WORKFLOW_OUTPUT"
fi
```
