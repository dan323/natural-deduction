# REST API Reference

This document describes the REST API endpoints for the Natural Deduction system.

## Endpoints

The server keeps no session. Every logic (`classical`, `modal`) is served under `/logic/{logic}`; the client sends
the whole proof with every request.

Every non-2xx response has the body `{"message": "..."}`. An unknown logic is a 404, malformed input a 400, a
solver that runs out of time a 422 and a solver that is already busy a 429.

### List the actions: `GET /logic/{logic}/actions`

Returns `200` with one descriptor per action the logic offers (`204` if a logic had none). `name` is what to send in `ActionDto.name`; `params` lists, in
order, the inputs the action needs.

```json
[
  {"name": "ANDI", "params": ["INT", "INT"]},
  {"name": "ASSUME", "params": ["EXPRESSION"]},
  {"name": "FE", "params": ["INT", "EXPRESSION"]},
  {"name": "DT", "params": []}
]
```

| Param kind   | Meaning                                | Where it goes in `POST /logic/{logic}/action` |
|--------------|----------------------------------------|-----------------------------------------------|
| `INT`        | a 1-based line number of the proof     | the next entry of `actionDto.sources`         |
| `EXPRESSION` | a formula                              | `actionDto.extraParameters.expression`        |
| `STATE`      | a state (world) name, modal logic only | `actionDto.extraParameters.state`             |

Classical logic has 14 actions (the `AvailableAction` names). Modal logic has 20; their names are the rule names of
the modal proof format (`Ass`, `|I1`, `->E`, `[]E`, `Refl`, ...). The list is built once at startup.

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
`"success": false` and the reason in `message` (the proof is returned unchanged) when it was well formed but does
not apply. A request without `actionDto` or `proofDto`, an unknown action name, a malformed expression or a proof
that is not valid is a `400`.

`done` says whether the goal of the returned proof is proved: the domain's `Proof.isDone()`, that is, some step at
assumption level 0 is the goal (not necessarily the last one). The UI shows it as the goal's success state.

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

### Upload a proof file: `POST /logic/{logic}/proof`

Multipart form with the file in the part `file`, in the layout `ProofStep.toString()` prints (3 spaces of indent per
assumption level, an 11-space gap, then the rule, e.g. `->I [1-2]`). Returns `201` with the `ProofDto`. A line that
cannot be parsed, or a step that does not follow, is a `400` whose message names the line.

## Other endpoints

- `GET /actuator/health` is the only Spring Actuator endpoint exposed over HTTP (Spring Boot's default). It is what
  the Docker `HEALTHCHECK` and the tests that boot the jar use.
- Every other path is served from the built frontend (`classpath:/public/`), when it was embedded in the jar.

There is no authentication, rate limiting beyond the solver's concurrency cap, or API versioning.

## References

- [Setup & Installation](./SETUP.md) - Run the application
- [Logical Languages](./LANGUAGES.md) - Formula specifications
- [Development Guide](./DEVELOPMENT.md) - Adding new endpoints
