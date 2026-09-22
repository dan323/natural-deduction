---
name: work-next-issue
description: >
  Process the next open GitHub issue in this repo end-to-end: sync open
  issues into a task-agent tasks.yml, run task-agent to open a PR for
  the top pending one, then run an independent reviewer/fixer loop
  against the PR (this repo has no Copilot review, so review-fix-loop
  stands in for copilot-review-fixer). Use when the user says "work
  on the next issue", "pick up the next task", "do the next thing in
  plan.md", "process the next issue", or similar. Composes
  sync-issues -> task-agent -> review-fix-loop via the workflow skill
  against ../../workflows/work-next-issue.yaml. This repo has no
  GitHub Projects board, so this is the board-free counterpart of the
  auto-board-task plugin skill.
tools: Bash
---

# Work Next Issue

Invoke the `workflow` skill **in the current conversation** on the bundled
[`work-next-issue.yaml`](../../workflows/work-next-issue.yaml).

## What to do

### Step 1 — Resolve arguments

Default `repo=dan323/natural-deduction` and `tasks=tasks.yml` (repo root). If the
user names a label ("work on the next 'ready' issue"), pass `label=<label>`.

### Step 2 — Invoke the workflow skill

```
Skill(skill="workflow", args="<repo-root>/.claude/workflows/work-next-issue.yaml repo=dan323/natural-deduction tasks=tasks.yml")
```

Use the absolute path to the YAML file (resolve `<repo-root>` from the current
working directory). Relay the workflow runner's per-step status block as-is.

## Do not

- Use the `Agent` tool to invoke the `workflow` skill — it must run inline via
  `Skill` so the runner's subagent spawns work correctly (same reason
  `auto-board-task` gives for this rule).
- Call `sync-issues`, `task-agent`, or `review-fix-loop` directly — the
  composition through the workflow file is the point.
- Substitute `copilot-review-fixer` back in — this repo has no Copilot review
  enabled; `review-fix-loop` is the intended replacement.
