# Improvement plan

The plan for the remaining work on the proof assistant. It started from a live audit of the UI (UX, accessibility,
performance, functional bugs) and grew into new logics. Out of scope: changes to the solver, except in PR 12 (solvers
for the logics that have none).

## Status (2026-09-29)

PRs 1-11 are merged; their steps are removed from this file (the issues and PRs below keep the details, and
`CHANGELOG.md` lists what they changed). PR 12 (solvers for intuitionistic, modal-next-until and first-order logic) is
pending: 12.1-12.3 are open as issues #176-#178. PR 13 (a responsive, modern UI) is pending: 13.1-13.4 are open as
issues #186-#189.

Merged so far:
- PRs 1-6 (UI audit fixes): 1-3, 4.1, 5.1, 5.2 and 5.4 as #124-#128, #130 and #131; 4.2 and 5.3 folded into #127 and
  #129; 5.5 (#122) as #138, 5.6 (#123) as #137, 6.1 (#132) as #135 and 6.2 (#133) as #134.
- PRs 7-9 (backend-checked proofs, exercises, intuitionistic logic): 7.1 (#139) as #151, 7.2 (#140) as #153, 7.3 (#141)
  as #156, 8.1 (#142) as #152, 8.2 (#143) as #157, 9.1 (#144) as #155, 9.2 (#145) as #158.
- PR 10 (modal logic in the UI): 10.1 (#146) as #154, 10.2 (#147) as #160, 10.3 (#148) as #159, 10.4 (#149) as #162,
  and 10.5 (#150), the `modal-next-until` logic, as #163 (backend) and #164 (frontend).
- PR 11 (first-order logic with equality and group theory): 11.1 (#170) as #179, 11.2 (#171) as #180, 11.3 (#172) as
  #181, 11.4 (#173) as #182, 11.5 (#174) as #183 and 11.6 (#175) as #184.
- Outside the plan: #165 (every logic accepts both the classical and the modal name of the 14 shared rules), #166,
  #167 and #169 (Sonar fixes, docs brought up to date) and #168 (pipeline, backend and frontend dependency bumps).

## How each step is written

Every step lists: **Change** (what and where), **Tests** (what to add or update), **Done when** (observable result).
Every PR must pass `npm run typecheck`, `npm test`, and, if it touches Java, `mvn -B verify`, locally before pushing,
and adds its entry to `CHANGELOG.md`.

---

## PR 12: Solvers for the logics that have none

This lifts "Out of scope: changes to the solver" for this PR only. Each step adds one solver behind the existing
`/solve` (timeout, concurrency cap and `done=false` for an unfinished proof all stay as they are), sets `hasSolver` to
true in `Transformer` and in `LOGICS`, and updates `docs/API.md` and `CLAUDE.md`. A solver emits only rules from its
logic's descriptor list, so its result replays through `Transformer.from` and Undo works on it. Like the existing
solvers, it starts from the premises (`proof.reset()`). `classical` and `modal` solve exactly as before.

**12.1 Intuitionistic solver (backend + one flag)** — pending (issue #176)
- Change: an `IntuitionisticNaturalDeduction` (a subclass of `NaturalDeduction`, which stops being `final`) whose
  `automate()` runs a new `IntuitionisticAutomate`. `ClassicalAutomate` cannot be reused: it proves goals by
  contradiction (`-I` then `-E`) and uses `DeMorgan`. The new solver is a complete search in the contraction-free
  sequent calculus G4ip, which always terminates. It translates the sequent proof into steps with `Ass`, `->I`, `->E`,
  `&I`, `&E1/2`, `|I1/2`, `|E`, `-I`, `FI`, `FE` and `Rep`, never `-E`. The intuitionistic `Transformer`/`ProofParser`
  create this proof type. `IntuitionisticProofTransformer.hasSolver()` returns true, and `LOGICS` sets
  `hasSolver: true` with a description that no longer says "There is no solver."
- Tests: every intuitionistic exercise is solved, the result contains no `-E` step, and it replays and is done.
  Unprovable goals (`p | -p`, `--p -> p`, `((p -> q) -> p) -> p`) end with `done=false` well within the timeout.
  `RestServiceIT`: `/logic/intuitionistic/solve` answers 200 (was 400). The classical solver tests are unchanged.
- Done when: Solve appears for intuitionistic logic in the UI and finishes every intuitionistic exercise.

**12.2 modal-next-until solver (backend + one flag)** — pending (issue #177)
- Change: open `ModalAutomate` up for extension (it drops `final` and gains protected hooks for intro and elimination
  rules, with its current behaviour as the default). A `ModalNextUntilAutomate` adds the following: for a goal `X A`
  in `s`, the goal `A` in `s+1`, then `XI`; for a goal `A U B`, try `UI1` (`B` in `s`), then `UI2` (`A` in `s`,
  `A U B` in `s+1`) up to a depth bound; elimination with `XE`, `UE` and `U<>`. It does not use `Ind`, because
  induction needs an invariant, so a goal that only `Ind` can prove ends with `done=false`.
  `ModalNextUntilNaturalDeduction.automate()` runs it, and `hasSolver` becomes true in the transformer and in `LOGICS`.
- Algorithm: the strategy above is a starting point. Searching online for a published proof-search algorithm for
  this logic is allowed, and one may be adapted in its place if it fits better, as long as it keeps the constraints
  of the shared context (only the logic's own rules, replays through `Transformer.from`, starts from the premises,
  stops before the timeout). Name the source in the PR description.
- Tests: every `modal-next-until` exercise whose reference solution has no `Ind` step is solved, replays and is done in
  `s0`. An `Ind` exercise ends with `done=false` before the timeout. `ModalFreshStateTest` and the `modal` solver tests
  are unchanged. `RestServiceIT`: `/logic/modal-next-until/solve` answers 200.
- Done when: Solve appears for modal-next-until and finishes its non-induction exercises.

**12.3 First-order solver (backend + one flag)** — pending (issue #178)
- Change: a best-effort `FirstOrderAutomate` (first-order logic is undecidable, so it can fail, and it ends with
  `done=false` when it does). It combines the classical propositional strategy with:
  - `∀I` or `∃E` with a fresh name when the goal or an assumption calls for it;
  - `∀E` or `∃I` instantiated with the terms already in the proof, up to a bounded term depth;
  - `=I`, and `=E` limited to symmetry and transitivity chains.

  `FirstOrderNaturalDeduction.automate()` runs it, and `hasSolver` becomes true in the transformer and in `LOGICS`.
- Algorithm: the strategy above is a starting point. Searching online for a published proof-search algorithm for
  this logic is allowed, and one may be adapted in its place if it fits better, as long as it keeps the constraints
  of the shared context (only the logic's own rules, replays through `Transformer.from`, starts from the premises,
  stops before the timeout). Name the source in the PR description.
- Tests: every pure (non-group) first-order exercise is solved, replays and is done. The group exercises are not
  required. A goal the solver cannot prove ends with `done=false` before the timeout. `RestServiceIT`:
  `/logic/first-order/solve` answers 200.
- Done when: Solve finishes `forall x. P(x) ⊢ exists x. P(x)` and the pure first-order exercises in the UI.

---

## PR 13: A responsive, modern UI

Frontend only, with no behaviour changes: every existing Jest test keeps passing, with only class-name selectors
updated. 13.1 is independent; 13.3 and 13.4 build on the tokens of 13.2.

**13.1 The New Proof dialog fits any window** — pending (issue #186)
- Change: `.modal` gets `padding: 16px` and `overflow: hidden`; `.modal-content` becomes a flex column with
  `max-height: calc(100dvh - 32px)`, whose title and `.modal-footer` stay put while only `.modal-body` scrolls
  (`overflow-y: auto`, `overscroll-behavior: contain`). Today `.modal-content` has neither a max height nor an overflow,
  so in a short window the Logic select and the Start Proof/Cancel footer are off-screen and cannot be scrolled to.
  Below 480px wide the dialog is full-screen, the footer buttons stack at full width and the remove-premise buttons are
  44×44px tap targets. The focus trap is unchanged; a focused field is scrolled into view inside the body.
- Tests: `NewProofModal.test.tsx`: the footer is not inside the scrolling body; Start Proof and Cancel stay reachable
  with Tab/Shift+Tab with 20 premises. jsdom does no layout, so a manual check at 360×640, 1280×500 and 1920×1080 with
  the group theory loaded and the load-text textarea filled.
- Done when: in a 1280×500 window with the group theory loaded, Start Proof and Cancel are visible without scrolling
  the page, and the premises scroll inside the dialog.

**13.2 Design tokens and one Button** — pending (issue #187)
- Change: `index.css` defines `:root` custom properties: a colour palette (primary, surface, border, text, muted,
  success, danger, warning), radii, a spacing scale, shadows, one UI font stack and one monospace stack for formulas
  (replacing the Arial of `Menu.css`/`Expressions.css` and the Courier New of the steps). A `components/ui/Button.tsx`
  on the already installed (and unused) `class-variance-authority`, with variants `primary`/`secondary`/`danger`/`ghost`
  and sizes `sm`/`md`, replaces the eight copies of the outlined button (`undo-btn`, `copy-text-btn`,
  `try-example-btn`, `browse-exercises-btn`, `exercise-start-btn`, `exercises-btn`, `exercises-close-btn`,
  `menu-button-secondary`) and the filled ones (`new-proof-btn`, `submit-btn`, `close-btn`, `menu-button`,
  `confirm-btn`, `cancel-btn`, `load-text-btn`). Every CSS file uses the tokens instead of hex values. The new look:
  softer surfaces, 8px radii, subtle shadows, and the header and toolbar merged into one sticky app bar.
- Tests: `Button.test.tsx` (variants, `disabled`, forwarded props and ref). Existing tests change only where they select
  by class name. A test that each text/background token pair has a contrast of at least 4.5:1.
- Done when: `grep -rE '#[0-9a-fA-F]{3,6}' frontend/src --include=*.css` finds colours only in the `:root` token block,
  and every button in the app is a `Button`.

**13.3 Responsive page layout** — pending (issue #188)
- Change: at 1024px and wider, the proof and the rule panel sit side by side (proof on the left, `Menu` sticky on the
  right), so applying a rule does not scroll the proof away; narrower, they stack as now. The proof table sits in an
  `overflow-x: auto` container, so a long formula scrolls inside it instead of the page. The goal uses
  `font-size: clamp(1.5rem, 4vw, 2.5rem)`. On phones the toolbar becomes a row of compact buttons and `ExerciseList`
  puts each Start button under its statement. (Today the frontend has no width `@media` query at all.)
- Tests: `App.test.tsx`: the proof and the Menu are in the new layout regions, with landmarks and labels unchanged.
  Manual check at 360, 768, 1280 and 1920px: no horizontal page scroll.
- Done when: at 360px wide the example proof causes no horizontal page scroll, and at 1280px the rule panel stays in
  view while scrolling a 30-line proof.

**13.4 Dark mode** — pending (issue #189)
- Change: a `prefers-color-scheme: dark` set of values for the 13.2 tokens, and a light/dark/system toggle in the app
  bar that sets `data-theme` on `<html>` and is saved in `localStorage` key `natural-deduction.theme` (unusable storage
  is ignored, like the other keys). The highlight glow, the subproof rules and the focus ring get dark values that stay
  distinguishable from each other.
- Tests: `theme.test.ts` (resolving system/light/dark, storage failure). `App.test.tsx`: the toggle sets `data-theme`.
  The 13.2 contrast test runs for both palettes.
- Done when: with the OS in dark mode the whole app (dialog and exercises included) is dark with readable text, and
  the toggle overrides it.

---

## Verification

- Jest tests next to each touched component; `npm run typecheck`; `mvn -B verify` for anything in Java.
- Performance is judged on the production build only: `mvn clean install` embeds the UI in the jar; run the jar and
  measure there (Vite dev-server timings are not meaningful).
- A manual pass with the keyboard only and with a screen reader on after UI changes: new proof, a rule to completion,
  an error, Undo, Solve.

## Risks and open ideas

- A solver must never emit a rule its logic does not offer (e.g. `-E` in intuitionistic logic), or its result will not
  replay; the replay tests in each step are what guard this.
- A solver that loops makes its PIT mutants time out; keep the depth bounds explicit and see the PIT notes in
  `CLAUDE.md`.
- The client-side formula checks (`checkFormula`, `checkFirstOrderFormula`) can drift from the Java parsers: keep them
  conservative.
- First-order equational proofs get long when every `=E` means typing the full target formula. A possible follow-up:
  `=E` computes the target from a chosen occurrence.
