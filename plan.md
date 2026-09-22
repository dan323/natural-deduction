# UI improvement plan

Based on a live audit of the UI (UX, accessibility, performance, functional bugs), a read of `frontend/src`, and checks
against the running backend. Out of scope for now: other logics in the UI (modal stays backend-only) and the solver.

Defaults taken for the open questions (change them and the affected steps move): the descriptor DTO **is** extended
(additively), formula input help is a **hint plus connective buttons** (no Unicode parsing), and **Phase 1 goes first**.

## Status (2026-09-22)

PRs 1-3, 4.1, 5.1 and 5.2 are merged (#124-#128, #130). 4.2 and 5.3 are also done, folded into #127 and #129 respectively,
without their own issue. 5.4 is open as #131 (#121, pending review). What is left is PR 5.5-5.6, tracked as #122 (5.5) and #123 (5.6).
PR 6.1 and 6.2 are new additions, tracked as #132 (6.1) and #133 (6.2).

## Audit summary

The core flow works (start proof, pick rule, enter lines, apply), but the UI uses little of what the model knows:

- The dropdown shows enum names (`ASSUME`, `ORI1`, `NOTE`, `FE`); the table shows `->E [1, 2]` for the same rule.
- Both line inputs are labelled "Line number:" although the order matters (`MP 2,1` fails, `MP 1,2` works).
- A failure never says why ("Rule DT cannot be applied").
- State leaks between proofs; invalid input is only caught by the backend ("Line -1 does not exist" for `a`, `1.5`, empty).
- Backend: `ASSUME` with an empty expression is a 500; an unparsable one returns `Cannot build action 'ASSUME': null`.
- Accessibility: weak or missing focus rings, contrast 3.5-4:1 on the blue buttons, green button and empty-state text,
  modal without focus trap, proof completion shown only by colour and confetti, cited lines highlighted on mouse hover only.
- The table shows `→ ∧ ∨ ¬`, the inputs accept only `-> & | !`, and nothing says so.

Findings I checked and rejected: "no error/loading feedback" (`Menu.tsx` has both) and "Apply Rule uses aria-disabled
only" (it also sets `disabled`, `Menu.tsx:183-184`). All Lighthouse timings (LCP 9.8 s, score 61) are Vite dev-server
artefacts; re-measure on the production build. The dev server that ran was Vite 6.4.1 while `package.json` says
`^8.3.0`: run `npm ci` before starting.

## How each step is written

Every step lists: **Change** (what and where), **Tests** (what to add or update), **Done when** (observable result).
Every PR must pass `npm run typecheck`, `npm test`, and, if it touches Java, `mvn -B verify`, locally before pushing.

---

## PR 1: Reset and validate the rule form (frontend only) — done (#124)

**1.1 Reset the Menu per proof**
- Change: in `App.tsx` add a `proofId` counter, bumped in `handleNewProofSubmit`, and render `<Menu key={proofId} …>`.
  That clears the selected rule, `GlowingInput` values, error, and notice. `onColorChange` glow is already cleared by
  `setColorMapping(new Map())`.
- Tests: `App.test.tsx`: select a rule, type inputs, start a new proof, expect rule and inputs empty and no alert.
- Done when: starting a second proof shows "-- Choose a rule --" and no stale error.

**1.2 Clear inputs after a successful apply**
- Change: `Menu.tsx` bumps a local `formKey` (used as `key` on the input container) and resets `sources/expression/state`
  on success. Keep the selected rule (students often apply the same rule again). Failure keeps the inputs.
- Tests: `Menu.test.tsx`: success clears inputs and keeps the rule; failure keeps both.
- Done when: pressing Apply twice cannot add the same step twice by accident.

**1.3 Client-side validation of line numbers**
- Change: `GlowingInput.tsx`: for `shouldGlow` inputs accept only `^\d+$`, otherwise set `aria-invalid="true"` and an
  `aria-describedby` message ("Enter a whole line number"); pass `undefined`/NaN, not `-1`, to `onInput`. `Menu.tsx`
  gets `stepCount = proof.steps.length` and flags values `< 1` or `> stepCount` ("The proof has N lines").
- Change: `Menu.tsx` computes `canApply` (rule chosen, every INT param valid, every EXPRESSION/STATE param non-blank) and
  uses it for `disabled`. Add a visible hint next to the disabled button ("Choose a rule", "Fill in all inputs").
- Tests: extend `MenuInputs.test.tsx` and `GlowingInputKinds.test.tsx` (invalid text, `0`, out of range, blank
  expression, Apply disabled/enabled). Backend is never called with `-1` (assert on the mocked `fetch` body).
- Done when: "Line -1 does not exist" can no longer be produced from the UI.

**1.4 New Proof modal validation and reset**
- Change: `NewProofModal.tsx`: reset `premises/goal` when it opens; before submit run a light syntax check
  (`service/utils.ts: checkFormula`: allowed characters, balanced parentheses, no dangling operator, not blank) and
  keep the modal open with an inline `role="alert"` message per field. Mark Goal `required`; explain the disabled
  Start Proof ("A goal is required"). After removing a premise, move focus to the neighbouring premise input.
- Note: the check is deliberately conservative; the backend stays the source of truth (a later optional step is a
  `POST /logic/{logic}/expression` parse endpoint so the modal can use the real parser).
- Tests: `NewProofModal.test.tsx` (`p ->`, `q &&`, unbalanced, blank goal, reopen resets, remove-premise focus);
  `utils.test.ts` for `checkFormula`.
- Done when: `p ->` never becomes a proof line.

**1.5 Small cleanups**
- Remove the duplicate empty-state text (keep the `App.tsx` one, drop the `Menu.tsx` paragraph).
- Fetch the action list once: module-level cache in `service/actions.ts` (promise keyed by logic) so StrictMode's
  double mount does not double-request; keep the `onError` path.
- Tests: `actions.test.ts` (second call does not refetch; a failed fetch is not cached).

---

## PR 2: Backend error hygiene (Java) — done (#125)

**2.1 Empty expression is a 400, not a 500**
- Investigate first: reproduce `ASSUME` with `expression: ""` and read the stack in the backend log (`buildAction`
  in `LogicalApplyAction.java:36-42` only wraps exceptions during action construction, so the 500 comes from a
  different point, probably a non-`RuntimeException` or a failure inside `isValid`/`apply`).
- Change: validate a blank expression for actions that need one and throw `InvalidActionException("… needs an expression")`.
- Tests: `ActionsUseCasesTest` / `ClassicalUseTest` (blank and whitespace-only expression -> `InvalidActionException`),
  `RestServiceIT` (400 with a message, not 500).

**2.2 No `null` in messages**
- Change: `LogicalApplyAction.java:40` builds the message from `e.getMessage()`; when it is null or blank use a
  fallback ("the expression could not be parsed"). Same for `ProofParser` paths if they share the pattern.
- Tests: the existing `assertEquals("Cannot build action 'Action1': No such action", …)` keeps passing; add a case
  with a null message.

---

## PR 3: Accessibility basics (CSS + small TSX) — done (#126)

**3.1 Focus and contrast**
- Change: replace every `outline: none` (`App.css:33`, `glowing.css:22`, `Menu.css:42,73,126`, `NewProofModal.css:66`)
  with a shared `:focus-visible { outline: 3px solid #0b5ed7; outline-offset: 2px }`; delete the inline
  `boxShadow: 'none'` in `GlowingInput.tsx` and drive the glow through a CSS custom property
  (`--glow-color`, as `StepViewer` already does) so focus and glow stay distinct.
- Change: darken `#007bff` -> `#0b5ed7`/`#0056b3`, `#28a745` -> `#1e7e34`, empty-state `#888` -> `#595959`,
  "+ Add Premise" text to `#0056b3`. Verify each pair >= 4.5:1.
- Done when: keyboard tabbing shows a visible ring on every control; a contrast check on the listed pairs passes.

**3.2 Modal as a real dialog**
- Change: `NewProofModal.tsx` uses `<dialog>` opened with `showModal()` (built-in focus trap, `Esc`, inert background)
  or, if jsdom support is a problem, a manual Tab trap plus `inert` on the app root. Keep return-focus behaviour.
- Tests: Tab from the last control wraps to the first; Esc closes; focus returns to New Proof.

**3.3 Confetti**
- Change: `Goal.tsx`: `aria-hidden="true"` on `.celebration`; `goal.css`: wrap the animation in
  `@media (prefers-reduced-motion: no-preference)`.

---

## PR 4: Rules that speak the model's language (backend + frontend together)

**4.1 Extend the descriptor, additively** — done (#127)
- Change (backend): `ActionDescriptorDto` (`domain/use-cases/model`) gains optional `label`, `symbol`, `category`
  (`INTRODUCTION` / `ELIMINATION` / `OTHER`), `description`, `paramLabels` (same length as `params`). Keep the
  existing `of(name, params…)` factory (modal keeps working with empty fields; `ModalGetActions` untouched for now).
  Fill them in `ClassicGetActions` in the same `switch` that already forces each rule to be described, e.g.:
  `MP -> "Modus ponens", "→E", ELIMINATION, "From A → B and A, derive B", ["Implication (A → B)", "Antecedent (A)"]`.
  Update `docs/API.md` (the actions section) and `ModelTest`, `ClassicalUseTest`.
- Change (frontend): `types.d.ts` gets the optional fields; `Menu.tsx` renders `<optgroup>` per category with
  `label (symbol)`, shows `description` under the select (`aria-describedby`), and uses `paramLabels[index]` for the
  input label with the current generic labels as fallback. `renderRule` in `utils.ts` and the descriptor `symbol` must
  produce the same name so a rule has one name in the dropdown and in the Rule column (derive one from the other or
  test that they agree).
- Tests: backend: every `AvailableAction` has a non-blank label, description and `paramLabels.size() == params.size()`.
  Frontend: grouped options, description text, per-input labels, fallback when fields are absent.
- Done when: choosing MP shows "Modus ponens (→E)", "From A → B and A, derive B", and inputs "Implication (A → B)" /
  "Antecedent (A)".

**4.2 Say why a rule did not apply** — done, folded into #127
- Change (first cut, frontend only): on a 202 failure append the descriptor's `description` to the message
  ("Rule MP cannot be applied to lines [2, 1]. MP needs A → B on the first line and A on the second.").
- Later, if wanted: a structured reason from the domain (`isValid` returning why) is a larger backend change.

---

## PR 5: Working on the proof itself

Order inside this PR series: 5.1, 5.3, 5.2, 5.4, 5.5, 5.6 (one small PR each).

**5.1 Click a row to fill a line input** (`ProofViewer.tsx`, `StepViewer.tsx`, `App.tsx`, `Menu.tsx`) — done (#128)
- Rows become focusable (`tabIndex=0`, Enter/Space activates); activating a row writes its number into the next empty
  INT input and triggers the existing glow. Fixes hover-only highlighting: `onFocus`/`onBlur` call the same handlers
  as mouse enter/leave. Needs lifting the "selected line" callback from `Menu` to `App` (the glow map already lives there).
- Tests: click and keyboard fill the first, then the second input; hover/focus highlight the cited rows.

**5.2 Show proof structure** (`StepViewer.tsx`, `Expressions.css`) — done (#130)
- Replace tab characters in `<pre>` with CSS indentation and a left rule per open subproof; line number becomes
  `<th scope="row">`; add a visually hidden "assumption level N"; mark discharged steps (the model `disable()`s them,
  check whether the DTO exposes that before promising it).

**5.3 Completion state** (`Goal.tsx`, `Menu.tsx`, `App.tsx`) — done, folded into #129
- When `proof.done`: a `role="status"` line "Proof complete" (also for the Solve path, which already has a notice),
  goal gets a text/icon marker not only green/red, rule controls are disabled, New Proof is offered. Confetti stays decoration.
- Tests: `Goal.test.tsx`, `Menu.test.tsx` for the done state.

**5.4 Undo and confirm** (`App.tsx`) — open as #131, pending review
- "Undo last step" resends the proof without its last step; safe because the server is stateless and replays the
  steps (check the discharge case: `DT`/`NOTI` mark earlier steps disabled, which is derived on replay, so this holds,
  but add a test against the real backend replay in `RestServiceIT` or a component test with a fixture).
- Confirm before "New Proof" replaces a proof that has more than its premises.

**5.5 Formula input help** (`NewProofModal.tsx`, `GlowingInput.tsx`) — open, tracked as #122
- Persistent syntax hint ("-> implies, & and, | or, ! not") and buttons that insert the connective at the cursor,
  labelled with the symbol shown in the table. Placeholders on expression inputs.

**5.6 Empty state** (`App.tsx`) — open, tracked as #123
- Single message, three-step "how it works", and a "Try an example" button that loads `p → q`, `p` with goal `q`.

---

## PR 6.1: Expose the proof text protocol in the UI — open, tracked as #132

- Change: the backend's `POST/GET /logic/{logic}/proof` text format (the fixed-indent `->I [1-2]` layout documented in
  `CLAUDE.md`) has zero frontend caller — `service/actions.ts` never calls it. Add a "Copy proof as text" action
  (fetches/renders it) and a "Load from text" input in `NewProofModal.tsx` or a new small panel, reusing the existing
  text layout the backend already parses.
- Tests: `actions.test.ts` for the new fetch calls; a component test round-tripping a small proof through copy →
  paste → load.
- Done when: a user can export the current proof as text and re-load it without going through the REST
  action-by-action flow.

---

## PR 6.2: Text equivalent for cited-line highlighting — open, tracked as #133

- Change: the "glow" that marks lines cited by the currently-focused rule inputs (`StepViewer.tsx`, added in PR 5.1)
  is color/box-shadow only (`glowStyle`/`--glow-color`). Add a visually-hidden text cue (e.g. `aria-describedby` or
  a `sr-only` span, "cited by current input") so screen-reader users get the same signal sighted users get from the
  glow.
- Tests: `StepViewer.test.tsx` — glowing row exposes the hidden text; non-glowing rows don't.
- Done when: a screen reader announces which row is cited without relying on color.

---

## Verification after all PRs

- Jest tests next to each touched component as listed; `npm run typecheck`; `mvn -B verify` for PRs 2 and 4.
- Production build: `npm run build`, embed into `executable/src/main/resources/public/`, run the jar, then re-run the
  site audit and Lighthouse there. Only then judge performance.
- Manual pass with the keyboard only and with a screen reader on: new proof, MP 1,2 to completion, an error, Undo.

## Risks

- PR 4 changes a REST payload shape: additive fields only, and `docs/API.md` must change in the same PR.
- The client-side formula check can drift from the real parser: keep it conservative, or add the parse endpoint.
- `<dialog>` under jsdom (Jest 30) has partial support; be ready to fall back to a manual focus trap.
- The Rule column renames rules in the display only; the `[1-2, 4]` range text that `ProofViewer` regex-parses must not change.
