---
name: plan-next-issues
description: >
  Plans the next round of work for this project: reads plan.md and the
  current codebase, proposes new PR-sized steps in plan.md's existing
  format, then (after user approval) files or updates matching GitHub
  issues in dan323/natural-deduction and updates plan.md's Status
  section to reference them. Use when the user says "plan more work",
  "what's next after this plan", "add more issues", "extend plan.md",
  "find more UI/backend work to do", or asks for the backlog to be
  refilled. This is the feeder for work-next-issue — it produces the
  issues that skill later picks up.
tools: Read, Edit, Bash, Glob, Grep
---

# Plan Next Issues

Feeds the `work-next-issue` queue. Where that skill *executes* the top pending
issue, this skill decides what the next issues *should be*.

**Arguments** (`key=value`, all optional):
- `repo=<owner/repo>` — default `dan323/natural-deduction`.
- `count=<n>` — target number of new steps to propose. Default `3`.
- `scope=<free text>` — narrows the audit (e.g. `scope=backend error handling`).
  Default: whatever plan.md's own stated scope is.

## Step 1 — Read the current plan and its stated scope/defaults

Read `plan.md` in full. Note:
- The **Out of scope** line near the top — never propose steps inside it unless
  the user's `scope=` argument explicitly overrides it.
- The **Status** section — which PRs are done/open, and by issue number.
- The numbering convention (`PR <n>: <title>`, sub-steps `<n>.<m>`) and the
  per-step template (**Change** / **Tests** / **Done when**) — new steps must
  match this shape exactly, no new structure invented.

## Step 2 — Read the current open issues (avoid duplicates)

```bash
gh issue list --repo "$REPO" --state open --json number,title,url
```

Cross-reference titles/numbers already mentioned in plan.md's Status line. Do not
propose a step that duplicates an open issue or an already-merged PR (check
`git log --oneline -20` and the Status section's "PRs ... are merged" line).

## Step 3 — Audit the codebase for the next round

Read enough of the affected areas (frontend `src/`, or backend modules, per
`scope=` or plan.md's own focus) to find concrete, PR-sized gaps — the same bar
plan.md's existing steps use: a specific change, specific tests, an observable
"done when". Do not propose vague items ("improve error handling" without saying
where/how). Ground every proposal in something you actually read (a file, a
missing test, a UX rough edge), not a generic best-practice.

Stop at `count=` proposals (default 3). Fewer, concrete proposals beat padding
to the count.

## Step 4 — Present proposals and get approval

Filing GitHub issues and editing plan.md are visible, shared-state changes —
**do not do either until the user approves.** Show the proposed steps in
plan.md's own format (a draft `## PR <n>: <title>` block per proposal, or a new
sub-step under an existing open PR heading if that's a better fit) and ask the
user to confirm, drop, or edit any before proceeding.

## Step 5 — On approval: update plan.md

Append the approved steps to plan.md following its existing structure exactly
(same heading level, same **Change**/**Tests**/**Done when** fields). Update the
**Status** line at the top to mention the new steps are pending (issue numbers
added once filed in Step 6).

## Step 6 — On approval: file or update GitHub issues

For each approved step, one issue:

```bash
gh issue create --repo "$REPO" \
  --title "<PR/step title, matching plan.md's heading>" \
  --body "<Change/Tests/Done-when from plan.md, plus a link back to the plan.md section>" \
  --label enhancement
```

If a step refines an *existing* open issue rather than adding a new one, use
`gh issue edit <number> --body "..."` instead of creating a duplicate.

After filing, go back and fill in the issue number(s) in plan.md's Status line
(`git commit`/push is the user's call — leave the plan.md edit staged/unstaged
per normal workflow, do not push automatically).

## Step 7 — Report

```
## Plan Next Issues — Done

Proposed: <n>, approved: <m>, filed as: #<a>, #<b>, ...
plan.md updated: <yes/no — which sections>
```

## Rules

- Never invent scope plan.md explicitly excludes (currently: other logics in the
  UI, and the solver) without the user overriding it via `scope=`.
- Never file an issue or edit plan.md before Step 4's approval — this skill's
  read/audit phases (1-3) are safe to run freely, its write phases (5-6) are not.
- Keep proposals PR-sized (one focused change), matching the granularity already
  in plan.md — not epics, not one-line nitpicks.
