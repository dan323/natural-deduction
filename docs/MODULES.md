# Project Modules

This document provides a detailed overview of each module in the Natural Deduction project.

## Module Hierarchy

```
natural-deduction (root pom.xml)
├── domain/
│   ├── logic-language/
│   │   ├── framework/
│   │   ├── implementation/
│   │   └── implementation.modal/
│   ├── proof-structures/
│   │   ├── framework.deduction/
│   │   ├── implementation.deduction.classic/
│   │   └── implementation.deduction.modal/
│   └── use-cases/
│       ├── base-use-case/
│       ├── classical-use-case/
│       ├── modal-use-case/
│       └── model/
├── executable/
├── rest/
│   ├── framework/
│   └── model/
└── jacoco-natural-deduction/ (code coverage aggregator)
```

## Domain Modules

### logic-language/

Defines how logical formulas are represented and parsed.

#### framework/
- **Purpose**: Generic formula AST shared by all logics
- **Key Concepts**:
  - `LogicOperation` - a formula
  - Generic operators: `Variable`, `Constant`, `Conjunction`, `Disjunction`, `Implication`, `Negation`, built on
    `UnaryOperation` / `BinaryOperation`
- **Dependencies**: None
- **Used By**: All logic language implementations and the proof modules
- **Java Package**: `com.dan323.expressions.base`

#### implementation/
- **Purpose**: Classical propositional logic
- **Key Components**:
  - `ClassicalLogicOperation` and one subclass per operator: `ConjunctionClassic`, `DisjunctionClassic`,
    `ImplicationClassic`, `NegationClassic`, `VariableClassic`, `ConstantClassic` (`TRUE`, `FALSE`)
  - `ClassicalParser`, built on javaluator. Syntax: `&`, `|`, `->`, `-` (negation), variables, `TRUE`/`FALSE`
- **Dependencies**: framework/, javaluator
- **Used By**: implementation.deduction.classic/
- **Java Package**: `com.dan323.expressions.classical`
- **Example Formula**: `(A | B) -> (-C & D)`

#### implementation.modal/
- **Purpose**: Modal propositional logic
- **Key Components**:
  - Modal counterparts of the classical operators (`ConjunctionModal`, ...) plus `Always` (`[]`), `Sometime` (`<>`)
    and `Until` (model only: the parser does not accept it)
  - Relation formulas between states: `LessEqual` (`<=`) and `Equals` (`=`)
  - `ModalLogicParser`, built on javaluator, and `ModalNextUntilLogicParser` (adds Next `X`, the `Next` formula, and
    Until `U`; states are `StateTerm`s such as `s0+1`)
- **Dependencies**: framework/, javaluator
- **Used By**: implementation.deduction.modal/
- **Java Packages**: `com.dan323.expressions` (parser), `com.dan323.expressions.modal`, `com.dan323.expressions.relation`
- **Example Formula**: `[](A -> B) & <>(A & -B)`

### proof-structures/

Defines and implements inference rules for natural deduction.

#### framework.deduction/
- **Purpose**: Generic proofs and rules
- **Key Concepts**:
  - `Proof<T,Q>` and `ProofStep` - a proof (goal, assumptions, steps) and its steps; steps are never removed on
    discharge, only disabled
  - `ProofReason` - the rule of a step and the lines it uses, printed and parsed as e.g. `->I [1-2]`
  - `Action` / `AbstractAction` - a rule: `isValid(proof)` checks it, `apply(proof)` adds the step
  - Generic rule bases: `AndI`, `AndE`, `OrI`, `OrE`, `ModusPonens`, `DeductionTheorem`, `NotI`, `NotE`, `FI`, `FE`,
    `Copy`, `Assume`
  - `Proof.automate()` - the automatic solver entry point
- **Dependencies**: logic-language/framework
- **Used By**: All deduction implementations and use cases
- **Java Packages**: `com.dan323.proof.generic`, `com.dan323.proof.generic.proof`

#### implementation.deduction.classic/
- **Purpose**: Classical natural deduction
- **Key Components**:
  - `NaturalDeduction` - the classical proof
  - One `Classic*` class per rule (`ClassicAndI`, `ClassicModusPonens`, ...), described by the `AvailableAction` enum
  - `ParseClassicalAction` - builds a rule from its name, sources and expression
  - The automatic solver (`ClassicalAutomate`) and a few composite rules (`complex/`, e.g. De Morgan)
- **Dependencies**: logic-language/implementation, framework.deduction/
- **Used By**: classical-use-case/
- **Java Packages**: `com.dan323.classical`, `com.dan323.classical.proof`

#### implementation.deduction.modal/
- **Purpose**: Modal natural deduction
- **Key Components**:
  - `ModalNaturalDeduction` - the modal proof; every step carries a state (world)
  - One `Modal*` class per rule (`ModalBoxE`, `ModalDiaI`, ...) and the relational rules `Reflexive` (`Refl`) and
    `Transitive` (`Trans`)
  - `ParseModalAction` - builds a rule from its name; it is the source of truth for the rule names
  - The automatic solver (`ModalAutomate`)
- **Dependencies**: logic-language/implementation.modal, framework.deduction/
- **Used By**: modal-use-case/
  - `com.dan323.proof.modal.nextuntil`: `ModalNextUntilNaturalDeduction` (successor-aware freshness, the goal must be
    in `s0`, no solver), the Next and Until rules (`ModalNextI`, `ModalUntilE`, `ModalInduction`, ...) and
    `ParseModalNextUntilAction`
- **Java Packages**: `com.dan323.proof.modal`, `com.dan323.proof.modal.proof`, `com.dan323.proof.modal.relational`,
  `com.dan323.proof.modal.nextuntil`

### use-cases/

Orchestrates the application logic by combining logic languages and proof structures.

#### model/
- **Purpose**: DTOs exchanged with clients
- **Key Classes**:
  - `ProofDto` (`steps`, `logic`, `goal`; serialized with a derived `done`), `StepDto`, `ActionDto`
  - `ActionDescriptorDto` (`name`, `params`) and `ParamKind` (`INT`, `EXPRESSION`, `STATE`): what
    `GET /logic/{logic}/actions` returns
- **Dependencies**: None
- **Used By**: All use cases and REST API
- **Java Package**: `com.dan323.model`

#### base-use-case/
- **Purpose**: Logic-independent use cases
- **Key Components**:
  - `ActionsUseCases` - list the actions, apply an action, solve, parse a proof file; `ApplyResult` carries `done`
  - `Transformer` (DTO <-> domain, by replaying the steps), `ProofParser` (proof file to proof),
    `LogicalGetActions` - the three pieces each logic provides
  - `LogicalApplyAction`, and `LogicalSolver` (runs `Proof.automate()` with a timeout and a cap on concurrent solves)
  - `UnknownLogicException`, `InvalidProofException`, `InvalidActionException`, `SolveTimeoutException`,
    `SolverBusyException`, which the REST layer maps to statuses
  - `ActionsUseCaseConfiguration` (Spring) - collects the beans of every logic by logic name
- **Dependencies**: logic-language/framework, proof-structures/framework.deduction, model/, Spring
- **Used By**: classical-use-case/, modal-use-case/, rest/framework
- **Java Packages**: `com.dan323.uses`, `com.dan323.uses.internal`

#### classical-use-case/
- **Purpose**: Wires classical logic (logic name `classical`)
- **Key Components**: `ClassicalProofTransformer`, `ParseClassicalProof`, `ClassicGetActions` (one descriptor per
  `AvailableAction`) and `ClassicalConfiguration`
- **Dependencies**: logic-language/implementation, proof-structures/implementation.deduction.classic, base-use-case/, model/
- **Used By**: executable/
- **Java Package**: `com.dan323.uses.classical`

#### modal-use-case/
- **Purpose**: Wires modal logic (logic name `modal`)
- **Key Components**: `ModalProofTransformer`, `ModalProofParser`, `ModalGetActions` and `AvailableModalAction` (the
  20 rule names with their inputs, including states), `ModalConfiguration`
- **Dependencies**: logic-language/implementation.modal, proof-structures/implementation.deduction.modal, base-use-case/, model/
- **Used By**: executable/
- Also wires `modal-next-until` (package `com.dan323.uses.modal.nextuntil`): `ModalNextUntilConfiguration`, the
  transformer, parser and actions subclass or extend the modal ones (`AvailableNextUntilAction` adds 8 actions), and
  `ModalNextUntilExercises`
- **Java Packages**: `com.dan323.uses.modal`, `com.dan323.uses.modal.nextuntil`

## REST and Executable Modules

### rest/

REST API contracts and models.

#### framework/
- **Purpose**: The REST controller and its error handling
- **Key Components**:
  - `ControllerInterface` - the only controller; serves every logic under `/logic/{logic}/actions|action|solve|proof`
  - `RestExceptionHandler` - turns every failure into an `ErrorResponse` (404 unknown logic, 400 invalid proof or
    action, 422 solver timeout, 429 solver busy, 500 otherwise)
- **Dependencies**: Spring Framework, base-use-case/, rest/model/
- **Used By**: executable/
- **Java Package**: `com.dan323.controller` (`ControllerInterface`, `RestExceptionHandler`)

#### model/
- **Purpose**: REST API models
- **Key Classes**:
  - `ProofActionRequest` - Input of `POST /logic/{logic}/action`: an `ActionDto` and the whole `ProofDto`
  - `ProofResponse` - Output of that endpoint: the proof, whether the action was applied (`success`), whether the goal is proved (`done`, decided by the domain's `Proof.isDone()`) and a `message` saying why it was not applied
  - `ErrorResponse` - Body `{"message": "..."}` of every non-2xx response
- **Dependencies**: domain/use-cases/model/
- **Used By**: executable/
- **Java Package**: `com.dan323.rest.model.*`

### executable/

The Spring Boot application that ties everything together.

- **Purpose**: Entry point and API server
- **Key Components**:
  - `Application` (Spring Boot entry point), `ApplicationConfiguration` (imports the configuration of every logic and
    scans the controller), `WebConfig` (serves `classpath:/public/`)
  - Integration tests (`*IT.java`, run by `mvn verify`), including one that boots the packaged fat jar, and
    `SpringVersionAlignmentTest`
- **Dependencies**: All domain modules, rest/, Spring Boot
- **Java Package**: `com.dan323.main`
- **Endpoints**: `/logic/{logic}/actions|action|solve|proof` for the logics `classical` and `modal`, see
  [API.md](./API.md); `/actuator/health`; the built frontend as static files, if it was embedded (see [SETUP.md](./SETUP.md))

## Frontend Module

### frontend/

React-based user interface.

- **Purpose**: Web UI for interactive proof building
- **Technology**: React 19, TypeScript 5, Vite, Jest
- **Key Components**:
  - `Menu` - the rule menu, built from the action descriptors, with the Apply Rule and Solve buttons
  - `ProofViewer` / `StepViewer` - the proof, with hover highlighting of the lines a rule uses
  - `Goal`, `NewProofModal`, `GlowingInput`, and `service/actions.ts` (the calls to `/logic/{logic}/...`)
- **Logic**: picked in the New Proof dialog (or on the empty page) from `LOGICS` in `src/constant.ts` (classical, intuitionistic, modal)
- **Location**: `frontend/src/`
- **Build**: `npm run build`
- **Type check**: `npm run typecheck`
- **Test**: `npm test`

## Build and Test Infrastructure

### jacoco-natural-deduction/

Aggregates code coverage reports from all modules.

- **Purpose**: Unified code coverage analysis
- **Tool**: JaCoCo
- **Report Location**: Generated after `mvn clean install`

## Module Dependencies

```
executable/
  └─→ rest/framework/
  └─→ rest/model/
  └─→ domain/use-cases/
      └─→ domain/use-cases/model/
      └─→ domain/use-cases/base-use-case/
      │   └─→ domain/logic-language/framework/
      │   └─→ domain/proof-structures/framework.deduction/
      └─→ domain/use-cases/classical-use-case/
      │   └─→ domain/logic-language/implementation/
      │   └─→ domain/proof-structures/implementation.deduction.classic/
      │       └─→ domain/proof-structures/framework.deduction/
      │       └─→ domain/logic-language/implementation/
      │           └─→ domain/logic-language/framework/
      └─→ domain/use-cases/modal-use-case/
          └─→ domain/logic-language/implementation.modal/
          │   └─→ domain/logic-language/framework/
          │   └─→ domain/logic-language/implementation/
          └─→ domain/proof-structures/implementation.deduction.modal/
              └─→ domain/proof-structures/framework.deduction/
              └─→ domain/proof-structures/implementation.deduction.classic/
```

## Building Specific Modules

```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl domain/logic-language/framework

# Build module and dependents
mvn clean install -amd -pl domain/logic-language/framework

# Run tests for specific module
mvn test -pl domain/logic-language/framework
```

## Adding a New Module

1. Create module directory following naming conventions
2. Create `pom.xml` with proper parent reference
3. Create `src/main/java` and `src/test/java` directories
4. Add module to parent `pom.xml` as `<module>`
5. Add dependencies to affected modules

## References

- [Architecture Overview](./ARCHITECTURE.md) - Component relationships
- [Development Guide](./DEVELOPMENT.md) - Contributing guidelines
- [Logical Languages](./LANGUAGES.md) - Logic specifications

