# Architecture Overview

## System Architecture

The Natural Deduction project follows a **plugin-based architecture** with clear separation between framework and implementations. This design allows for easy extension with new logical systems without modifying core framework code.

```
┌─────────────────────────────────────────────────────┐
│              Frontend (React)                        │
│         Web UI for proof building                    │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│         REST API Layer (Spring Boot)                 │
│  Serves HTTP endpoints for proof operations          │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│         Domain Layer (Use Cases)                     │
│  Business logic and orchestration                    │
└────────────────────┬────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼──────────────┐  ┌──────▼────────────────────┐
│ Logic Language Layer │  │ Proof Structures Layer     │
│ Defines logical terms│  │ Inference rules            │
└───────┬──────────────┘  └──────┬────────────────────┘
        │                        │
   ┌────┴────────────┐      ┌────┴──────────────────┐
   │                 │      │                       │
┌──▼──┐  ┌──────────▼──┐  ┌▼─────────┐  ┌─────────▼──┐
│Base │  │ Classical   │  │ Classical  │  │   Modal    │
│Fwk  │  │ Logic       │  │ Deduction  │  │ Deduction  │
└─────┘  │ Impl        │  │            │  │            │
         └─────────────┘  └────────────┘  └────────────┘
```

## Layered Architecture

### 1. Frontend Layer
- **Technology**: React 19, TypeScript
- **Responsibility**: User interface for proof construction and verification
- **Location**: `frontend/`
- **Features**:
  - Goal/formula input
  - Proof visualization
  - Rule selection (built from the action descriptors of the backend) and application
  - A logic selector (in the New Proof dialog and on the empty page) offering the logics of `LOGICS` in `constant.ts`: classical, intuitionistic, modal and modal with Next and Until; everything logic-specific (rules, exercises, formula help) follows the logic of the proof on screen
  - A Solve button that asks the backend's automatic solver to finish the proof, for the logics that have one
  - Undo, "Copy proof as text" and "Load from text" (a finished proof in the proof-text layout)
  - An exercise list (`ExerciseList`, from `GET exercises`) grouped by difficulty, with a "Solved n of m" counter, a "(Solved)" marker per exercise and a "Next exercise" button; starting an exercise over a proof with more than its premises asks for confirmation before discarding it (again, if the proof changed while the exercise was being checked by the backend); an exercise answer arriving after the user asked for another proof is dropped, and so is a rule or solver answer arriving after its proof was replaced, so it can never be shown or marked solved as another exercise
  - Browser storage: the proof on screen is kept in `sessionStorage` under `natural-deduction.proof` (goal, steps, logic and the `exerciseId` it was started from, if any) so it survives a reload; the solved exercises are kept in `localStorage` under `natural-deduction.solved-exercises`, as a list of exercise ids per logic. Storage that cannot be used is ignored

### 2. REST API Layer
- **Technology**: Spring Boot 3.5.16
- **Responsibility**: HTTP interface to business logic
- **Location**: `executable/`, `rest/`
- **Endpoints** (all under `/logic/{logic}`, see [API.md](./API.md)):
  - `GET actions`: the rules the logic offers, as typed descriptors
  - `GET exercises`: the logic's exercises (without their solutions)
  - `POST action`: apply a rule to a proof
  - `POST solve`: run the automatic solver (with a timeout)
  - `POST proof`: parse an uploaded proof file
- **Errors**: invalid requests and domain failures (unknown logic, malformed proof or action, solver timeout or overload) are thrown and mapped by `RestExceptionHandler` to an `ErrorResponse` (`{"message": ...}`) with a fitting status
- **Action that does not apply**: a well-formed action that the proof rejects is not an error. `POST action` answers `202` with a `ProofResponse` (`success=false`, the request's proof as the server replayed it, without the action, and a `message`), see [API.md](./API.md)
- **Stateless**: the client sends the whole proof with every request; the server replays its steps to rebuild it

### 3. Domain Layer
- **Responsibility**: Core business logic and use cases
- **Location**: `domain/`
- **Subdivisions**:
  - **Logic Language Layer**: Defines formulas and logical terms
  - **Proof Structures Layer**: Implements deduction rules and proof verification

## Modular Design

The project uses a **plugin architecture** to support different logical systems:

### Framework Modules
These define the core interfaces and abstractions:

- **logic-language/framework** - Abstract interfaces for logical terms
- **proof-structures/framework.deduction** - Abstract interfaces for deduction rules

### Implementation Modules
These provide concrete implementations:

- **logic-language/implementation** - Classical propositional logic (also used by intuitionistic logic)
- **logic-language/implementation.modal** - Modal propositional logic, and its Next and Until extension
- **proof-structures/implementation.deduction.classic** - Classical deduction rules
- **proof-structures/implementation.deduction.modal** - Modal deduction rules, and the Next and Until rules

### Use Cases
Apply the logical systems to solve problems:

- **base-use-case** - Common functionality
- **classical-use-case** - Wires `classical`, and `intuitionistic` as a filter on it (every rule but double negation elimination)
- **modal-use-case** - Wires `modal`, and `modal-next-until` as an extension of it (more rules and connectives)
- **model** - Data models

## Component Interaction Flow

```
User Input (Frontend)
        ↓
REST Controller (rest/framework)
        ↓
Use Case (domain/use-cases)
        ↓
        ├─→ Logic Language (Process formulas)
        │
        └─→ Proof Structures (Apply rules)
                ├─→ Rule Framework
                │
                └─→ Specific Logic Implementation
                        (classical or modal)
        ↓
Response (JSON via REST)
        ↓
Frontend Display
```

## Separation of Concerns

### Logic Framework (language-framework)
**Interface**: Defines what a logical term must provide
**Purpose**: Establish contracts for logical expressions

### Logic Implementations
**Concrete Classes**: Classical and modal logic term implementations
**Purpose**: Provide actual implementations of logical operators and parsing

### Deduction Framework (proof-structures)
**Interface**: Defines deduction rule structure
**Purpose**: Establish contracts for proof rules

### Deduction Implementations
**Concrete Classes**: Classical and modal deduction rules
**Purpose**: Implement actual proof rules for each logical system

## Data Flow for Proof Verification

```
Input: Formula String
        ↓
        Parse using Logic Language Implementation
        ↓
        Convert to Formula objects
        ↓
        Initialize proof goal
        ↓
        User applies deduction rules (via frontend)
        ↓
        Validate rule application using Proof Structures
        ↓
        Update proof tree
        ↓
        Check if proof is complete
        ↓
        Return result to frontend
```

## Key Design Patterns

### 1. Strategy Pattern
Different logic implementations (classical, intuitionistic, modal, modal-next-until) are interchangeable strategies.

### 2. Registry by Dependency Injection
Each logic exposes a `Transformer`, a `ProofParser` and a `LogicalGetActions` bean, and may expose a `LogicalExercises`
bean; `ActionsUseCaseConfiguration` collects them into maps keyed by logic name, and the `{logic}` path segment selects
the entry.

### 3. Template Method Pattern
Framework classes define the structure of algorithms, implementations fill in specific steps.

### 4. Adapter Pattern
Use cases adapt between the REST API and domain logic.

## Extensibility Points

To add a new logical system:

1. Add a language module (subclass the operators of `logic-language/framework`, write a parser)
2. Add a proof module (bind `proof-structures/framework.deduction` to the language: a proof class, one class per
   rule, a parser of rule names and, optionally, an automatic solver)
3. Add a use-case module exposing the `Transformer`, `ProofParser` and `LogicalGetActions` beans (and optionally a
   `LogicalExercises` catalog) and a `*Configuration`. A logic can also reuse another one's modules and filter its rules
   (like `intuitionistic`) or extend them (like `modal-next-until`)
4. Import that configuration in `executable/.../ApplicationConfiguration` (no new controller is needed)
5. Extend the frontend if it should use the new logic (add it to `LOGICS` in `constant.ts`)

See [Development Guide](./DEVELOPMENT.md) for detailed instructions.

## Deployment Architecture

The application is containerized using Docker (the image only contains the jar, so the frontend must be embedded in it
before packaging; the image runs as a non-root user and has a health check on `/actuator/health`):

```
┌─────────────┐
│   Docker    │
│   Container │
│             │
│ ┌─────────┐ │
│ │ Backend │ │
│ │ Spring  │ │
│ │ Boot    │ │
│ └────┬────┘ │
│      │       │
│ ┌────▼────┐ │
│ │Frontend │ │
│ │(Static) │ │
│ └─────────┘ │
└─────────────┘
```

The executable Spring Boot application serves both the REST API and static frontend files. On every push to `master`,
CI tests the code, builds the image from the tested jar, smoke-tests it and then publishes it to Docker Hub.

## References

- See [Project Modules](./MODULES.md) for detailed module descriptions
- See [Setup & Installation](./SETUP.md) for build and deployment information

