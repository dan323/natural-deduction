# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Natural deduction proof assistant for classical and modal propositional logic: Java 21 / Spring Boot backend (multi-module Maven) plus a React 19 / TypeScript / Vite frontend. `docs/` has longer prose docs (`docs/API.md` is the REST reference); when they disagree with the code or the poms, trust the code.

## Commands

Backend (run from repo root):

```bash
mvn -B clean install                 # build everything + unit tests + ITs
mvn -B verify                        # what CI runs (unit tests, then *IT.java in the executable module, incl. the one that boots the packaged fat jar)
mvn -pl domain/proof-structures/implementation.deduction.classic -am test                 # one module (-pl takes directory paths)
mvn -pl domain/proof-structures/implementation.deduction.classic -am test -Dtest=ClassicAndTest   # one test class
mvn -pl executable -am verify -Dtest=RestServiceIT -Dsurefire.failIfNoSpecifiedTests=false        # the Spring integration test
mvn -B test-compile org.pitest:pitest-maven:mutationCoverage    # mutation testing (CI job; skipped inside `executable`)
java -jar executable/target/executable-0.1-SNAPSHOT.jar         # serves the API on :8080 (and the UI, if it was embedded, see below)
```

Frontend (from `frontend/`):

```bash
npm ci                        # use ci, not install; node_modules can be left over from the old react-scripts setup
npm start                     # vite dev server; proxies /logic to localhost:8080, so start the backend jar too
npm test                      # jest --coverage
npx jest src/components/menu  # a single test file/dir
npm run typecheck             # tsc --noEmit; `vite build` does not type-check
npm run build                 # outputs to frontend/build (NOT dist)
```

CI (`.github/workflows/`) has no lint step. Run the full Maven `verify` and `npm run typecheck` + `npm test` locally before pushing.

## Architecture

**Backend layering** (`domain/` is framework-agnostic except the use-case config classes, which are Spring `@Configuration`s):

- `domain/logic-language/`: formula ASTs. `framework` defines generic operators (`Conjunction`, `Implication`, `Negation`, ...); `implementation` (classical) and `implementation.modal` subclass them and each provide a parser (built on javaluator). Modal adds `Always`/`Sometime`/`Until` and relation formulas (`Equals`, `LessEqual`).
- `domain/proof-structures/`: `framework.deduction` has the generic `Proof<T,Q>`, `ProofStep`, `ProofReason`, `Action`/`AbstractAction` and the generic rule bases (`AndI`, `ModusPonens`, `DeductionTheorem`, ...). `implementation.deduction.classic` / `.modal` bind those to a concrete language: `NaturalDeduction` / `ModalNaturalDeduction`, one `Classic*`/`Modal*` class per rule, `ParseClassicalAction`/`ParseModalAction`, and an automatic solver. Rules validate with `isValid(proof)` then mutate with `apply(proof)`. Steps are never removed on discharge, only `disable()`d.
- `domain/use-cases/`: `model` (DTOs `ProofDto`/`StepDto`/`ActionDto`/`ActionDescriptorDto`), `base-use-case` (`ActionsUseCases`, `Transformer` DTO<->domain, `ProofParser` text->proof, `LogicalApplyAction`, `LogicalSolver`, the exceptions, and `ActionsUseCaseConfiguration`), plus `classical-use-case` and `modal-use-case`.
- `rest/`: `framework` has the single `ControllerInterface` (`/logic/{logic}/actions|action|solve|proof`) and `RestExceptionHandler`; `model` has the request/response records (`ProofActionRequest`, `ProofResponse`, `ErrorResponse`).
- `executable/`: Spring Boot app. It also serves the built frontend as static files from `classpath:/public/`.

**How a new logic plugs in:** each logic's use-case module exposes `Transformer`, `ProofParser` and `LogicalGetActions` beans, keyed by a logic-name string (`"classical"`, `"modal"`). `ActionsUseCaseConfiguration` collects all beans of those types into maps keyed by `logic()`. The URL `{logic}` path segment picks the entry. So adding a logic means adding modules plus a `*Configuration` that is imported in `executable/.../ApplicationConfiguration`. A logic may also expose a `LogicalExercises` catalog (`GET .../exercises`; a known logic without one answers `[]`). Each `Exercise` keeps a reference solution in the proof-text layout that is never sent to clients; `ClassicalExercisesTest` replays every one, so a new exercise must come with a solution that parses and is done.

**Statelessness:** the server keeps no session. For every action the client sends the whole `ProofDto` plus an `ActionDto`. `Transformer.from(ProofDto)` rebuilds the domain proof by replaying each step's rule, then applies the new action and serializes back.

**Actions and the solver:**
- `GET .../actions` returns `ActionDescriptorDto`s (`name` + ordered `params` of `ParamKind` `INT`/`EXPRESSION`/`STATE`), built once at startup. Classical: `ClassicGetActions` maps every `AvailableAction` (its `switch` has no default, so a new action does not compile until described). Modal: `AvailableModalAction`, whose names are the rule names `ParseModalAction.parseAction` understands. `ActionDto.name` must equal a descriptor name. `INT` params are the 1-based `ActionDto.sources`, in order; `EXPRESSION` goes in `extraParameters.expression`, `STATE` in `extraParameters.state`. The frontend `Menu` renders its inputs from the descriptor.
- `POST .../action` answers 200 with `ProofResponse(proof, success, done, message)`, or 202 with `success=false` when the action is well formed but does not apply (the returned proof is the replayed request proof without the action: same steps, but assumption levels recomputed by the domain, whatever levels the request sent; the frontend's `replayProof` (`service/actions.ts`: undo, the New Proof check and the restore after a reload) relies on this). `done` is the domain's `Proof.isDone()` (a top-level step equals the goal, not necessarily the last); the UI reads it instead of computing success itself.
- `POST .../solve` runs `Proof.automate()` via `LogicalSolver` on a daemon thread with a timeout (`natural-deduction.solve-timeout`, default 10s) and a cap on concurrent solves. An unfinished proof is still a 200 with `done=false`.
- Failures are `ErrorResponse{message}` from `RestExceptionHandler`: 404 unknown logic, 400 `InvalidProofException`/`InvalidActionException`, 422 `SolveTimeoutException`, 429 `SolverBusyException`, 500 for anything else.

**Text protocols that span layers (edit these together):**
- Proof text files (`POST .../proof`) and `ProofStep.toString()` use a fixed layout: 3 spaces of indent per assumption level, an 11-space gap, then the rule, e.g. `->I [1-2]`. `ProofParser.ProofLine`/`parseLine` and `ProofReason.parseReason` depend on this. Frontend `proofToText` (`service/utils.ts`, "Copy proof as text") writes it, and `loadProofFromText` (`service/actions.ts`, "Load from text" in `NewProofModal`) posts it to this endpoint, so the UI only loads finished proofs: the last line is the goal, the leading top-level `Ass` lines are the premises. Frontend `ProofViewer` also regex-parses the `[1-2, 4]` part of rule strings for hover highlighting.
- Line numbers in rules and in `ActionDto.sources` are 1-based.

**Java modules (JPMS):** every module has a `module-info.java`. A new package that another module needs must be `exports`ed, and new dependencies need a `requires`. Spring itself runs on the classpath, but the modules still have to compile.

## Frontend / build gotchas

- The frontend calls relative URLs (`/logic/...`). `vite.config.ts` proxies `/logic` (only that path) to `http://localhost:8080`, so `npm start` works next to a running backend jar. `constant.ts` only holds `LOGIC`.
- The UI is hardcoded to the `"classical"` logic (`LOGIC` in `constant.ts`); modal is backend-only.
- To embed the UI in the jar (what `OnMaster.yml` does): `npm run build`, copy `frontend/build/*` into `executable/src/main/resources/public/` (gitignored), then `mvn -pl executable -am clean package` (always with `clean`: `executable/target/classes` keeps the old `public/` files otherwise, and the build cannot replace the jar while it is running, so stop the server first). A stale `public/` from an earlier build gets packaged as is; empty it before copying (`rm -rf executable/src/main/resources/public/*`), or files of the old build stay in the jar next to the new ones.
- Backend `executable` runs `*IT.java` via a second surefire execution bound to `verify`, so plain `mvn test` skips them. `FatJarActionsIT` starts the packaged jar, so it only works through `verify`.
- The root pom's `spring.version` must match the Spring version that the Spring Boot parent of `executable/pom.xml` manages; `SpringVersionAlignmentTest` fails otherwise (e.g. after a dependabot Boot bump).
- `executable` does not inherit the root pom, so JUnit there comes from the Boot parent. Its `junit-jupiter.version` property overrides Boot's JUnit 5 to match the root `junit5.version` (JUnit 6). Bump both together.

## CI / Docker

- `CompileAndTest.yml`: `mvn verify` + Sonar + JaCoCo report, and a separate PIT mutation job (PIT settings live in the root pom's `pitest-maven` pluginManagement: `threads` 4 and a short `timeoutConstant`, because the mutants that make the automatic solvers loop forever each cost a full timeout; raise the timeout if healthy mutants ever show up as `TIMED_OUT`). `frontend.yml`: `npm ci`, `npm run typecheck`, `npm test`, Sonar. `OnMerge.yml` removes a closed PR's reports from gh-pages.
- `OnMaster.yml` ("Publish Docker image"): typechecks and tests the frontend, embeds it, runs `mvn verify`, then builds the image from that exact jar, smoke-tests it and only then pushes to Docker Hub. The smoke test checks `/logic/classical/actions`, `/logic/modal/actions`, the UI and that the container user is not root.
- The `Dockerfile` copies `executable/target/*.jar`, runs as a non-root user and has a `HEALTHCHECK` on `/actuator/health`.
