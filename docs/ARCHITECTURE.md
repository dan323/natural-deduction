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
  - A Solve button that asks the backend's automatic solver to finish the proof
  - Hardcoded to the classical logic

### 2. REST API Layer
- **Technology**: Spring Boot 3.5.3
- **Responsibility**: HTTP interface to business logic
- **Location**: `executable/`, `rest/`
- **Endpoints** (all under `/logic/{logic}`, see [API.md](./API.md)):
  - `GET actions`: the rules the logic offers, as typed descriptors
  - `POST action`: apply a rule to a proof
  - `POST solve`: run the automatic solver (with a timeout)
  - `POST proof`: parse an uploaded proof file
- **Errors**: every failure is an `ErrorResponse` (`{"message": ...}`) with a fitting status, see `RestExceptionHandler`
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

- **logic-language/implementation** - Classical propositional logic
- **logic-language/implementation.modal** - Modal propositional logic
- **proof-structures/implementation.deduction.classic** - Classical deduction rules
- **proof-structures/implementation.deduction.modal** - Modal deduction rules

### Use Cases
Apply the logical systems to solve problems:

- **base-use-case** - Common functionality
- **classical-use-case** - Uses classical logic
- **modal-use-case** - Uses modal logic
- **model** - Data models

## Component Interaction Flow

```
User Input (Frontend)
        ↓
REST Controller (executable)
        ↓
Use Case (domain/use-cases)
        ↓
        ├─→ Logic Language (Process formulas)
        │
        └─→ Proof Structures (Apply rules)
                ├─→ Rule Framework
                │
                └─→ Specific Logic Implementation
                        (Classical or Modal)
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
Different logic implementations (classical vs modal) are interchangeable strategies.

### 2. Registry by Dependency Injection
Each logic exposes a `Transformer`, a `ProofParser` and a `LogicalGetActions` bean; `ActionsUseCaseConfiguration`
collects them into maps keyed by logic name, and the `{logic}` path segment selects the entry.

### 3. Template Method Pattern
Framework classes define the structure of algorithms, implementations fill in specific steps.

### 4. Adapter Pattern
Use cases adapt between the REST API and domain logic.

## Extensibility Points

To add a new logical system:

1. Add a language module (subclass the operators of `logic-language/framework`, write a parser)
2. Add a proof module (bind `proof-structures/framework.deduction` to the language: a proof class, one class per
   rule, a parser of rule names, an automatic solver)
3. Add a use-case module exposing the `Transformer`, `ProofParser` and `LogicalGetActions` beans and a `*Configuration`
4. Import that configuration in `executable/.../ApplicationConfiguration` (no new controller is needed)
5. Extend the frontend if it should use the new logic (`LOGIC` in `constant.ts`)

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

