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
- **Technology**: React 18, TypeScript
- **Responsibility**: User interface for proof construction and verification
- **Location**: `frontend/`
- **Features**:
  - Goal/formula input
  - Proof tree visualization
  - Rule selection and application
  - Real-time validation

### 2. REST API Layer
- **Technology**: Spring Boot 3.3.2
- **Responsibility**: HTTP interface to business logic
- **Location**: `executable/`, `rest/`
- **Endpoints**:
  - Proof verification
  - Rule suggestions
  - Proof serialization/deserialization

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

### 2. Factory Pattern
Logic language and proof structure implementations are created via factories, allowing runtime selection.

### 3. Template Method Pattern
Framework classes define the structure of algorithms, implementations fill in specific steps.

### 4. Adapter Pattern
Use cases adapt between the REST API and domain logic.

## Extensibility Points

To add a new logical system:

1. Implement the **LogicLanguage** framework interface
2. Implement the **DeductionRule** framework interface
3. Create a use case that orchestrates both
4. Add REST controller endpoint
5. Extend frontend to support new operators (if needed)

See [Development Guide](./DEVELOPMENT.md) for detailed instructions.

## Deployment Architecture

The application is containerized using Docker:

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

The executable Spring Boot application serves both the REST API and static frontend files.

## References

- See [Project Modules](./MODULES.md) for detailed module descriptions
- See [Setup & Installation](./SETUP.md) for build and deployment information

