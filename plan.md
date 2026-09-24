# UI improvement plan

Based on a live audit of the UI (UX, accessibility, performance, functional bugs), a read of `frontend/src`, and checks
against the running backend. Out of scope for now: changes to the solver. Intuitionistic (PR 9) and modal (PR 10) logic
are brought to the UI.

Defaults taken for the open questions (change them and the affected steps move): the descriptor DTO **is** extended
(additively), formula input help is a **hint plus connective buttons** (no Unicode parsing), and **Phase 1 goes first**.

## Status (2026-09-24)

PRs 1-6 are merged: 1-3, 4.1, 5.1, 5.2 and 5.4 as #124-#128, #130 and #131; 4.2 and 5.3 folded into #127 and #129;
5.5 (#122) as #138, 5.6 (#123) as #137, 6.1 (#132) as #135 and 6.2 (#133) as #134.
PR 7.1-7.3 are pending, tracked as #139 (7.1), #140 (7.2) and #141 (7.3).
PR 8.1-8.2 and 9.1-9.2 are pending, tracked as #142 (8.1), #143 (8.2), #144 (9.1) and #145 (9.2).
PR 10.1-10.5 are pending, tracked as #146 (10.1), #147 (10.2), #148 (10.3), #149 (10.4) and #150 (10.5).
Order: 7.x first (8.2 starts exercises through the 7.1 path), then 8.1, 8.2, 9.1, 9.2, then 10.1-10.5 (10.4 needs 9.2's
logic selector; 10.5's UI part needs 10.3 and 10.4).

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

**5.4 Undo and confirm** (`App.tsx`) — done (#131)
- "Undo last step" resends the proof without its last step; safe because the server is stateless and replays the
  steps (check the discharge case: `DT`/`NOTI` mark earlier steps disabled, which is derived on replay, so this holds,
  but add a test against the real backend replay in `RestServiceIT` or a component test with a fixture).
- Confirm before "New Proof" replaces a proof that has more than its premises.

**5.5 Formula input help** (`NewProofModal.tsx`, `GlowingInput.tsx`) — done (#138, issue #122)
- Persistent syntax hint ("-> implies, & and, | or, ! not") and buttons that insert the connective at the cursor,
  labelled with the symbol shown in the table. Placeholders on expression inputs.

**5.6 Empty state** (`App.tsx`) — done (#137, issue #123)
- Single message, three-step "how it works", and a "Try an example" button that loads `p → q`, `p` with goal `q`.

---

## PR 6.1: Expose the proof text protocol in the UI — done (#135, issue #132)

- Change: the backend's `POST/GET /logic/{logic}/proof` text format (the fixed-indent `->I [1-2]` layout documented in
  `CLAUDE.md`) has zero frontend caller — `service/actions.ts` never calls it. Add a "Copy proof as text" action
  (fetches/renders it) and a "Load from text" input in `NewProofModal.tsx` or a new small panel, reusing the existing
  text layout the backend already parses.
- Tests: `actions.test.ts` for the new fetch calls; a component test round-tripping a small proof through copy →
  paste → load.
- Done when: a user can export the current proof as text and re-load it without going through the REST
  action-by-action flow.

---

## PR 6.2: Text equivalent for cited-line highlighting — done (#134, issue #133)

- Change: the "glow" that marks lines cited by the currently-focused rule inputs (`StepViewer.tsx`, added in PR 5.1)
  is color/box-shadow only (`glowStyle`/`--glow-color`). Add a visually-hidden text cue (e.g. `aria-describedby` or
  a `sr-only` span, "cited by current input") so screen-reader users get the same signal sighted users get from the
  glow.
- Tests: `StepViewer.test.tsx` — glowing row exposes the hidden text; non-glowing rows don't.
- Done when: a screen reader announces which row is cited without relying on color.

---

## PR 7: Backend-checked proofs

**7.1 Check a new proof's formulas with the backend before showing it** — open, tracked as #139
- Change: `NewProofModal` keeps `checkFormula` as the instant check, but on submit `App.handleNewProofSubmit` sends the
  premises-only proof (premises + goal) through the existing `replayProof` (`service/actions.ts`, the path Undo uses;
  the transformers parse the goal and every step). A rejection keeps the dialog open with the backend's message; success
  shows the proof as the backend returned it. "Try an example" goes through the same path. No new endpoint, no backend
  change. Answers the drift risk of 1.4 without the parse endpoint that step mentioned.
- Tests: `App.test.tsx`: a formula `checkFormula` accepts but the mocked backend rejects keeps the dialog open with the
  message; an accepted proof is the one the backend returned; a late answer after Cancel is ignored.
- Done when: a formula the backend cannot parse never becomes a proof line.

**7.2 Keep the proof across a page reload** (`App.tsx`) — open, tracked as #140
- Change: nothing in `frontend/src` uses storage today, so a reload loses the proof. Save the current `ProofDto` to
  `sessionStorage` on every change (read and write wrapped in try/catch); on mount, restore it through `replayProof`
  so the backend re-checks it and gives back `done`. A rejected or corrupt saved proof falls back to the empty state with
  a notice. New Proof and "Try an example" overwrite it.
- Tests: `App.test.tsx`: a saved proof is restored and replayed; a corrupt or rejected one shows the empty state; a
  storage that throws is ignored; a new proof replaces the saved one.
- Done when: reloading mid-proof shows the same proof with the same done state.

**7.3 "Load from text" accepts only finished proofs, through `POST .../proof`** — open, tracked as #141
- Change: "Load from text" posts the text to `POST /logic/{logic}/proof`, which already takes the last line as the goal,
  the leading `Ass` lines as the premises, and rejects a proof that does not end at the top level or has a step that does
  not follow. Remove the dialog's goal field, `parseProofText` and the replay-based `loadProofFromText` (keep
  `proofToText`). The copy notice no longer asks the user to note the goal; for a proof that is not done it says the text
  only loads back once the proof is finished. `CLAUDE.md` "Text protocols": drop the `parseProofText` note, say the UI
  loads text through this endpoint.
- Tests: `actions.test.ts` (the POST call, error handling); `NewProofModal.test.tsx` (no goal field, error shown for an
  unfinished proof); `App.test.tsx` (a finished proof round-trips copy → load; an unfinished one is rejected);
  remove the `parseProofText` tests from `utils.test.ts`.
- Done when: pasting a finished proof rebuilds it with its goal and nothing else to type; an unfinished or invalid one
  says why it cannot load.

---

## PR 8: Exercises

**8.1 An exercise catalog per logic (backend)** — open, tracked as #142
- Change: `GET /logic/{logic}/exercises` returns `ExerciseDto(id, title, premises, goal, difficulty)` from a per-logic
  bean, collected into a map by `logic()` like the other use-case beans. The classical set has about 12 exercises from
  easy to hard (MP chains, ∧/∨ elimination, →I, ¬I, `--p ⊢ p`, `⊢ p | -p`). Each exercise keeps a reference solution in
  the proof-text layout, which is never sent to the client. Update `docs/API.md`.
- Tests: a unit test runs every reference solution through `ProofParser`: it parses, its premises and last line equal
  the exercise's, and it is done. `RestServiceIT`: 200 with the list, 404 for an unknown logic.
- Done when: every listed exercise is provable, and a test fails if one is not.

**8.2 Exercises in the UI (frontend)** — open, tracked as #143
- Change: an "Exercises" list, reachable from the empty state and the toolbar, grouped by difficulty. "Start" opens the
  exercise through the 7.1 path (backend-checked). Solved exercises are recorded in `localStorage` (wrapped in try/catch)
  and shown with a text marker, not colour alone. When a proof started from an exercise is done, the completion notice
  offers "Next exercise".
- Tests: `actions.test.ts` (fetch and cache); `App.test.tsx`: starting an exercise shows its premises and goal;
  finishing it marks it solved, which survives a remount; "Next exercise" starts the next one.
- Done when: a student can work through the list and see which exercises they have solved.

---

## PR 9: Intuitionistic logic

**9.1 Intuitionistic propositional logic (backend)** — open, tracked as #144
- Investigate first: intuitionistic = classical without double negation elimination (`NOTE`, `ClassicNotE`, "¬E");
  `FE` (ex falso) stays. Choose between a thin `implementation.deduction.intuitionistic` / `intuitionistic-use-case` pair
  reusing the classical language and rule classes with its own `NaturalDeduction` subclass and action enum without
  `NOTE`, or a rule filter on the classical use case; keep the no-default `switch` guarantee of `ClassicGetActions`.
  Check whether `automate()` uses `NOTE`; if so `POST /logic/intuitionistic/solve` answers 400 "no solver for this
  logic" instead of producing a classical proof.
- Change: register `"intuitionistic"` (`Transformer`, `ProofParser`, `LogicalGetActions` without `NOTE`) through a
  `*Configuration` imported in `ApplicationConfiguration`, with the `module-info` exports/requires. Update `docs/API.md`,
  add `/logic/intuitionistic/actions` to the `OnMaster.yml` smoke test, and add an intuitionistic exercise set (8.1).
- Tests: the actions have no `NOTE`, and `NOTE` sent as an action is a 400; the proof of `--p ⊢ p` does not replay
  under intuitionistic but does under classical; `RestServiceIT` for the endpoints and the solve answer.
- Done when: `GET /logic/intuitionistic/actions` lists every classical rule except ¬E, and a classical-only proof is
  rejected.

**9.2 Pick the logic in the UI (frontend)** — open, tracked as #145
- Change: `LOGIC` in `constant.ts` becomes the list of logics the UI supports (classical, intuitionistic; modal stays
  backend-only). The New Proof dialog gets a logic selector and the current logic is shown next to the goal. Everything
  that uses `LOGIC` (`Menu`, `actions.ts`, 7.2's saved proof, 8.2's list) reads it from `proof.logic` instead.
- Tests: an intuitionistic proof fetches its own rules (no ¬E); switching logic resets the Menu; a restored proof
  keeps its logic; the exercise list follows the logic.
- Done when: a student can do the same exercise in both logics and see that `--p ⊢ p` only works classically.

---

## PR 10: Modal logic in the UI

The backend is complete (`/logic/modal/actions|action|solve|proof`); the UI has pieces already (`Menu` renders `STATE`
inputs and sends `extraParameters.state`, `renderExpression` shows `[]`/`<>` as □/◇) but cannot run a modal proof yet.

**10.1 Describe the modal rules (backend)** — open, tracked as #146
- Change: `AvailableModalAction` builds its descriptors with `ActionDescriptorDto.of(name, params)` only, so the modal
  rules have no `label`, `symbol`, `category`, `description` or `paramLabels` (PR 4.1 left `ModalGetActions` alone).
  Fill them for all 20 rules as `ClassicGetActions` does, in a form that fails to compile or fails a test when a rule is
  added without a description (e.g. `□I` "Box introduction", `◇E`, `Refl`, `Trans`, and `STATE` param labels such as
  "State (e.g. s1)"). Symbols must agree with `renderRule` on the rule strings the modal steps carry (`[]I`, `-E`, ...).
  Update `docs/API.md`.
- Tests: every modal descriptor has a non-blank label and description and `paramLabels.size() == params.size()`; the
  existing test that every entry builds an action keeps passing.
- Done when: `GET /logic/modal/actions` describes each rule as the classical endpoint does.

**10.2 States in modal proofs (frontend)** — open, tracked as #147
- Change: `ModalProofTransformer.initialAssumption` rejects a premise whose `extraParameters.state` is not the initial
  state `s0`, and `handleNewProofSubmit` sends `{}`. For a modal proof, premises get `{ state: 's0' }` (investigate
  first what relation premises such as `s0 <= s1` need; `initialAssumption` only checks logical formulas). `StepViewer`
  shows each step's state (`extraParameters.state`) in its own column, with a header and text, not colour alone;
  `proofToText` / the text protocol keep working for modal (check `ModalProofParser`'s line format for the state).
- Tests: `App.test.tsx` / `StepViewer.test.tsx`: a modal proof's premises carry `s0`; the state column shows each step's
  state and is absent for classical proofs; an Apply with a `STATE` input round-trips against a mocked modal backend.
- Done when: a modal proof started in the UI survives its first action, and every row says which state it is in.

**10.3 Modal formula help (frontend)** — open, tracked as #148
- Change: the #138 hint and connective buttons cover only `→ ∧ ∨ ¬`. For a modal proof add `□` (`[]`) and `◇` (`<>`)
  buttons and hint entries, and explain relation formulas (`s0 <= s1`, `s0 = s1`) in the hint. `checkFormula` already
  knows these tokens; add tests for them. Note: `Until` (`U`) exists in the model but `ModalLogicParser` has no operator
  for it, so it cannot be typed; leave it out of the help.
- Tests: `GlowingInputConnectives.test.tsx` / `NewProofModal.test.tsx`: modal buttons appear only for modal proofs and
  insert `[]`/`<>`; `utils.test.ts`: `checkFormula` on `[]p -> p`, `<>(p & q)`, `s0 <= s1`, and a dangling `[]`.
- Done when: every modal formula can be typed with the buttons, and the hint lists every operator the parser accepts.

**10.4 Offer modal logic in the selector (frontend + exercises)** — open, tracked as #149
- Change: add `"modal"` to the logics of 9.2's selector once 10.1-10.3 are in; add a modal exercise set to 8.1's
  catalog (e.g. `[]p ⊢ p` with `Refl`, `[]p ⊢ [][]p` with `Trans`, `p ⊢ <>p`), each with a reference solution checked by
  the same `ProofParser` test. Extend the `OnMaster.yml` smoke test only if it does not already cover modal actions (it
  does today).
- Tests: `App.test.tsx`: choosing modal fetches the modal rules, shows the state column and modal buttons; the modal
  exercise list loads; backend: the modal reference solutions replay.
- Done when: a student can pick modal logic, start a modal exercise and finish it in the UI.

**10.5 A `modal-until` logic: modal logic with `Until` (backend, then UI)** — open, tracked as #150
- Idea: keep `"modal"` as it is and add a new logic, `ModalWithUntil` (URL key `"modal-until"`), that is modal logic plus
  `Until`. `Until` (`expressions/modal/Until.java`, printed as `A U B`) already exists in the formula model and is handled
  generically (`RuleUtils`, `DeductionTheorem`, `NotI`, `ModalBoxI`, `ModalDiaE`), but `ModalLogicParser` has no `U`
  operator and no rule introduces or eliminates it.
- Investigate first: how to extend rather than copy the modal modules. Likely a parser subclass of `ModalLogicParser`
  that adds `U` (check that javaluator does not split variables containing a capital `U`, or pick another symbol), a
  `ModalWithUntilNaturalDeduction` (or reuse `ModalNaturalDeduction`) and an action enum = `AvailableModalAction` + the
  Until rules. Decide `Until`'s meaning in the state semantics the modal rules use (states ordered by `<=`, as `□`/`◇`
  and `Refl`/`Trans` use them) and design its rules, e.g. `UI`: from `B` at `s`, derive `A U B` at `s`; from `A` at `s`
  and `A U B` at a successor, derive `A U B` at `s`; `UE` by cases. Check whether `automate()` copes with `U`; if not,
  `POST /logic/modal-until/solve` answers 400 "no solver for this logic".
- Change (backend): new `implementation.deduction.modal-until` / `modal-until-use-case` modules (or a sub-package, per
  the investigation) with `Transformer`, `ProofParser`, `LogicalGetActions` (10.1-style descriptions) and a
  `*Configuration` imported in `ApplicationConfiguration`; `module-info` exports/requires; `docs/API.md`; the
  `OnMaster.yml` smoke test gets `/logic/modal-until/actions`. `"modal"` does not change: `p U q` is still rejected there.
- Change (frontend, after 10.3 and 10.4): `"modal-until"` in the logic selector; a `U` connective button and hint entry
  for that logic only; `checkFormula` accepts `U`; a few exercises in 8.1's catalog.
- Tests: the parser round-trips `p U q` and its precedence against `->`/`&`; `"modal"` still rejects `p U q`; each Until
  rule's valid and invalid cases; a small proof using `U` replays through `ProofParser`; `RestServiceIT` for the new
  logic's endpoints; frontend: the `U` button shows only for `modal-until` and inserts `U`.
- Done when: `/logic/modal-until/...` proves formulas with `A U B` from the REST API and the UI, and `/logic/modal/...`
  behaves exactly as before.

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
