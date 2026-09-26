# REST API Reference

This document describes the REST API endpoints for the Natural Deduction system.

## Endpoints

The server keeps no session. Every logic (`classical`, `intuitionistic`, `modal`, `modal-next-until`) is served under `/logic/{logic}`; the client sends
the whole proof with every request.

Every non-2xx response has the body `{"message": "..."}`. An unknown logic is a 404, malformed input a 400, a
solver that runs out of time a 422 and a solver that is already busy a 429.

### List the actions: `GET /logic/{logic}/actions`

Returns `200` with one descriptor per action the logic offers (`204` if a logic had none). `name` is what to send in `ActionDto.name`; `params` lists, in
order, the inputs the action needs.

```json
[
  {"name": "ANDI", "params": ["INT", "INT"], "label": "And introduction", "symbol": "∧I", "category": "INTRODUCTION",
   "description": "From A and B, derive A ∧ B", "paramLabels": ["Line with A", "Line with B"]},
  {"name": "MP", "params": ["INT", "INT"], "label": "Modus ponens", "symbol": "→E", "category": "ELIMINATION",
   "description": "From A → B and A, derive B", "paramLabels": ["Implication (A → B)", "Antecedent (A)"]},
  {"name": "[]E", "params": ["INT", "INT"], "label": "Box elimination", "symbol": "□E", "category": "ELIMINATION",
   "description": "From □A in state s and s <= t, derive A in state t",
   "paramLabels": ["Necessity (□A in state s)", "Relation (s <= t)"]}
]
```

The first two are from `/logic/classical/actions`, the last from `/logic/modal/actions`.

| Param kind   | Meaning                                | Where it goes in `POST /logic/{logic}/action` |
|--------------|----------------------------------------|-----------------------------------------------|
| `INT`        | a 1-based line number of the proof     | the next entry of `actionDto.sources`         |
| `EXPRESSION` | a formula                              | `actionDto.extraParameters.expression`        |
| `STATE`      | a state (world) name, modal logic only | `actionDto.extraParameters.state`             |

The other fields only help a client to present the action. They are optional: a logic that does not provide them
sends `null` (`[]` for `paramLabels`), and a client falls back to `name` and to a generic label per param kind.

| Field         | Meaning                                                                                                          |
|---------------|------------------------------------------------------------------------------------------------------------------|
| `label`       | human name of the rule, e.g. `Modus ponens`                                                                      |
| `symbol`      | how the rule is written in a proof, e.g. `→E`. It is the rule text of a step (`->E [1, 2]`) as the frontend renders it |
| `category`    | `INTRODUCTION`, `ELIMINATION` or `OTHER`, for grouping                                                           |
| `description` | one sentence saying what the rule does, without a final full stop                                                |
| `paramLabels` | one label per entry of `params`, in the same order (for `INT` params, the order of `actionDto.sources`), or empty |

The order of the `INT` params matters: `MP` needs the implication first and the antecedent second.

Classical logic has 14 actions (the `AvailableAction` names). Modal logic has 20; their names are the rule names of
the modal proof format (`Ass`, `|I1`, `->E`, `[]E`, `Refl`, ...). Both logics fill every presentation field for every
action. The modal `symbol`s follow the same rule, e.g. `□I` for a step `[]I [2-4]`, and `Refl`/`Trans` for the
relation rules (category `OTHER`). Modal descriptions write the reachability of states as it is typed, `s <= t`, and
the `STATE` params of `Ass` and `FE` are labelled `State of A (e.g. s1)`; an `Ass` of a relation formula such as
`s0 <= s1` ignores its state. The list is built once at startup.

The 14 rules the two share can be sent under either name to every logic that has the rule: classical, intuitionistic,
modal and `modal-next-until` logic all accept both `COPY` and `Rep`, `MP` and `->E`, `ASSUME` and `Ass`, and so on (the
classical `AvailableAction` names and the modal rule names). Intuitionistic logic has no double negation elimination,
so it rejects both `NOTE` and `-E`. Errors name the action as it was sent, e.g.
`Rule -E is not a rule of intuitionistic logic`.

> **Breaking change:** this endpoint used to return strings such as `"ANDI([int, int])"`.

### Apply an action: `POST /logic/{logic}/action`

```json
{
  "actionDto": {"name": "COPY", "sources": [1], "extraParameters": {"expression": ""}},
  "proofDto": {
    "logic": "classical", "goal": "Q -> P",
    "steps": [{"expression": "P", "rule": "Ass", "assmsLevel": 0, "extraParameters": {}}]
  }
}
```

`200` with `{"proof": {...}, "success": true, "done": false, "message": ""}` when the action was applied, `202` with
`"success": false` and the reason in `message` (the proof is returned without the action, as the server replayed it: same steps, but the
assumption levels are the ones the rules imply, whatever levels the request sent) when it was well formed but does
not apply. The UI appends the `description` of the rule to that message. A request without `actionDto` or `proofDto`, an unknown action name, a malformed expression or a proof
that is not valid is a `400`.

`done` says whether the goal of the returned proof is proved: the domain's `Proof.isDone()`, that is, some step at
assumption level 0 is the goal (not necessarily the last one). The UI shows it as the goal's success state. In modal
logic that check does not look at the step's state yet, so the goal formula derived at level 0 in a state other than
`s0` also counts as done (a known limitation).

### Solve a proof: `POST /logic/{logic}/solve`

Runs the automatic solver on the proof (a `ProofDto`) and returns the resulting `ProofDto`.

```json
{"logic": "classical", "goal": "P -> P", "steps": []}
```

```json
{
  "logic": "classical", "goal": "P -> P", "done": true,
  "steps": [
    {"expression": "P", "rule": "Ass", "assmsLevel": 1, "extraParameters": {}},
    {"expression": "P -> P", "rule": "->I [1-1]", "assmsLevel": 0, "extraParameters": {}}
  ]
}
```

- `done` is the same verdict as in the response of `/action`, derived from the steps by the same rule.
- A proof the solver cannot finish (unprovable, or beyond what the solver can do) is still a `200`: the proof comes
  back as far as it got and `done` is `false`.
- The solver is limited by time, because a user triggers it. When it does not finish within the limit (10 seconds by
  default, set with the property `natural-deduction.solve-timeout`, e.g. `natural-deduction.solve-timeout=5s`) the
  solver thread is interrupted and the response is `422` with
  `{"message": "The solver did not finish within 10 seconds, try solving part of the proof by hand first"}`.
- At most as many solves as there are processors (at least 2) run at once, per logic. Another one is answered at once
  with `429` and `{"message": "The solver is busy with other proofs, try again in a moment"}`.
- An invalid proof is a `400`, as for `/action`.
- A logic without a solver of its own (`intuitionistic`, `modal-next-until`) answers `400` with
  `{"message": "There is no solver for the logic 'intuitionistic'"}`.

### List the exercises: `GET /logic/{logic}/exercises`

Returns `200` with the logic's exercises, ordered from easy to hard. Each one asks to prove `goal` from `premises`.

```json
[
  {"id": "modus-ponens", "title": "Modus ponens", "premises": ["p", "p -> q"], "goal": "q", "difficulty": "EASY"},
  {"id": "excluded-middle", "title": "The law of excluded middle", "premises": [], "goal": "p | (- p)", "difficulty": "HARD"}
]
```

- `id` is stable and unique within the logic. `difficulty` is `EASY`, `MEDIUM` or `HARD`.
- The formulas are written the way the server prints them (fully parenthesized, `- (- p)` rather than `--p`, which
  does not parse), so they can be sent back as they are and match the expressions of the proof's steps.
- Classical logic has 14 exercises, intuitionistic logic 16 (the classical ones except `double-negation-elimination`
  and `excluded-middle`, plus four of its own), modal logic 7 (with premises in `s0`, e.g. `box-elimination`, `[] p` to
  `p`, and `box-transitive`, `[] p` to `[] ([] p)`), `modal-next-until` 9, from `next-in-and-out` (`X p ⊢ X (p | q)`)
  to `always-always-next` (`[] p ⊢ X ([] p)`). A known logic without exercises answers `200` with `[]`; an unknown
  logic is a `404`.
- Every exercise has a reference solution on the server, a proof in the proof-file layout (see below) that a unit test
  replays (for modal logic it also checks that the goal is derived in `s0`, which `done` does not look at). It is
  never sent to the client.

### Upload a proof file: `POST /logic/{logic}/proof`

Multipart form with the file in the part `file`, in the layout `ProofStep.toString()` prints (3 spaces of indent per
assumption level, an 11-space gap, then the rule, e.g. `->I [1-2]`). Returns `201` with the `ProofDto`. A line that
cannot be parsed, or a step that does not follow, is a `400` whose message names the line. In a `modal` file a line
that is in a state starts with the state and `: `, before the indent (`s0: [] p           Ass`,
`s1:    q           Ass`); a relation between states (`s0 <= s1           Ass`) has no prefix. A `modal-next-until` file uses the same layout,
with successor states (`s0+1: p           XE [1]`); a file that does not prove its goal (the last line) in `s0`, such
as one that ends with `s0+1: p`, is a `400`.

### Intuitionistic logic

`intuitionistic` is classical logic without double negation elimination (`NOTE`, the rule `-E`, shown as ¬E). It
shares everything else with `classical`: the formula syntax, the proof and proof-file formats, the other rules
(including ex falso, `FE`) and their descriptors.

- `GET /logic/intuitionistic/actions` lists every classical action except `NOTE`, with the same descriptors.
- `NOTE` sent to `POST /logic/intuitionistic/action` is a `400` (`Rule NOTE is not a rule of intuitionistic logic`).
- A proof (in a request, or an uploaded file) with a step justified by `-E` is a `400` whose message names that line,
  e.g. the classical proof of `- (- p) ⊢ p`.
- `POST /logic/intuitionistic/solve` is a `400`: the classical solver may use double negation elimination, so its
  proofs are not necessarily intuitionistic.

### Modal logic with Next and Until

`modal-next-until` is modal logic over discrete time: every state `s` has a successor `s+1`, and `s <= t` holds when
`t` is `s`, `s+1`, `s+2`, ... It adds two connectives to the modal formulas: Next, `X A` (`A` holds in `s+1`), and
Until, `A U B` (`B` holds in some `s+k` and `A` in every state from `s` up to, not including, `s+k`). `"modal"` does not
change.

- Formulas: `X` is a unary connective like `-`, `[]` and `<>`, and any of these can follow another (`X - p`,
  `[] X p`). `U` binds tighter than `&`, `|` and `->` and looser than the unary connectives, and groups to the left:
  `- p U X q -> r` is `((- p) U (X q)) -> r`. `X` and `U` are operators only as words of their own, so `TRUE`, `Xp`
  and `pUq` are names, and no variable can be called `X` or `U`. The server prints `X p`, `p U q`, `X (p U q)`,
  `(X p) U q`.
- States are a name followed by successor steps: `s0`, `s0+1`, `s0+2`. The server keeps them written without spaces
  and with one `+k` at most (`s0 + 1 + 1` is kept as `s0+2`, `s0+0` as `s0`), in `extraParameters.state` and in both
  sides of a relation (`s0 <= s0+1`). A state that is not like this (`s0-1`, `s0+`), or whose offset does not fit in
  an `int` (`s0+99999999999`), is a `400`. The last one, `s0+2147483647`, has no written successor: `XE` and `Succ`
  on a line in it do not apply (`202`).
- `GET /logic/modal-next-until/actions` lists the 20 modal actions, with the same descriptors, then:

  | name   | params     | rule text    | what it does                                                        |
  |--------|------------|--------------|---------------------------------------------------------------------|
  | `XI`   | INT        | `XI [i]`     | from `A` in `s+1`, derive `X A` in `s`                              |
  | `XE`   | INT        | `XE [i]`     | from `X A` in `s`, derive `A` in `s+1`                              |
  | `Succ` | INT        | `Succ [i]`   | from any formula in `s`, derive the relation `s <= s+1`             |
  | `UI1`  | INT, EXPR  | `UI [i]`     | from `B` in `s`, derive `A U B` in `s` (`A` is the expression)      |
  | `UI2`  | INT, INT   | `UI [i, j]`  | from `A` and `X (A U B)` in `s`, derive `A U B` in `s`              |
  | `UE`   | INT        | `UE [i]`     | from `A U B` in `s`, derive `B \| (A & X (A U B))` in `s`           |
  | `U<>`  | INT        | `U<> [i]`    | from `A U B` in `s`, derive `<> B` in `s`                           |
  | `Ind`  | INT, INT   | `Ind [i, j]` | from `A` and `[] (A -> X A)` in `s`, derive `[] A` in `s`           |

- `XI` needs a line whose state is written as a successor: `A` in `s1` does not give `X A` anywhere, even if some
  earlier line says `s0 <= s1`.
- The fresh state of `[]I` and `<>E` must be a new name, not `s0+1` (the successor of `s0` is not arbitrary), and no
  earlier line may use it or any of its successors; nor may the other side of its relation (`t+1 <= t`). `<>E`
  accepts a last line in any state whose name is used before, such as `s0+3`.
- A proof is done when a top-level line is the goal in `s0` (or the goal is a relation): `X p ⊢ p` is not done by
  `p` in `s0+1`. In `modal`, `done` still looks at the formula only.
- `POST /logic/modal-next-until/solve` is a `400`: the modal solver uses none of the Next and Until rules.

## Other endpoints

- `GET /actuator/health` is the only Spring Actuator endpoint exposed over HTTP (Spring Boot's default). It is what
  the Docker `HEALTHCHECK` and the tests that boot the jar use.
- Every other path is served from the built frontend (`classpath:/public/`), when it was embedded in the jar.

There is no authentication, rate limiting beyond the solver's concurrency cap, or API versioning.

## References

- [Setup & Installation](./SETUP.md) - Run the application
- [Logical Languages](./LANGUAGES.md) - Formula specifications
- [Development Guide](./DEVELOPMENT.md) - Adding new endpoints
