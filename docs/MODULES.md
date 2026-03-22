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

Defines how logical formulas are represented and manipulated.

#### framework/
- **Purpose**: Abstract interfaces and contracts for logical expressions
- **Key Concepts**:
  - `LogicLanguage` interface - Contract for logical terms
  - `Term` interface - Represents a logical formula
  - `TermFactory` - Creates terms from strings
- **Dependencies**: None
- **Used By**: All logic language implementations and use cases
- **Java Package**: `com.dan323.logic.language.*`

#### implementation/
- **Purpose**: Classical propositional logic implementation
- **Key Components**:
  - Classical operators: AND (∧), OR (∨), NOT (¬), IMPLIES (→), BICONDITIONAL (↔)
  - Propositional variables (A, B, C, etc.)
  - Formula parsing and construction
- **Dependencies**: framework/
- **Used By**: classical-use-case/
- **Java Package**: `com.dan323.logic.language.implementation.*`
- **Example Formula**: `(A ∨ B) → (¬C ∧ D)`

#### implementation.modal/
- **Purpose**: Modal propositional logic implementation
- **Key Components**:
  - Classical operators (from implementation/)
  - Modal operators: □ (necessity/always), ◇ (possibility/possibly)
  - Labeled formulas with world states
  - State relationships
- **Dependencies**: framework/, implementation/
- **Used By**: modal-use-case/
- **Java Package**: `com.dan323.logic.language.implementation.modal.*`
- **Example Formula**: `□(A → B) ∧ ◇(A ∧ ¬B)`

### proof-structures/

Defines and implements inference rules for natural deduction.

#### framework.deduction/
- **Purpose**: Abstract interfaces for deduction rules
- **Key Concepts**:
  - `DeductionRule` interface - Contract for inference rules
  - `Proof` interface - Represents a proof state
  - `ProofChecker` - Validates proof correctness
  - Rule types: introduction and elimination rules
- **Dependencies**: logic-language/framework
- **Used By**: All deduction implementations and use cases
- **Java Package**: `com.dan323.proof.structures.deduction.*`

#### implementation.deduction.classic/
- **Purpose**: Classical natural deduction rules
- **Key Rules**:
  - **Conjunction**: ∧-intro, ∧-elim
  - **Disjunction**: ∨-intro, ∨-elim
  - **Implication**: →-intro, →-elim (modus ponens)
  - **Negation**: ¬-intro, ¬-elim (contradiction)
  - **Biconditional**: ↔-intro, ↔-elim
- **Dependencies**: logic-language/implementation, framework.deduction/
- **Used By**: classical-use-case/
- **Java Package**: `com.dan323.proof.structures.deduction.implementation.*`
- **Rule Application**: Pattern matching on formula structure, maintaining proof context

#### implementation.deduction.modal/
- **Purpose**: Modal natural deduction rules
- **Key Rules**:
  - All classical rules (inherited)
  - **Necessity**: □-intro, □-elim
  - **Possibility**: ◇-intro, ◇-elim
  - **State handling**: Label propagation and management
- **Dependencies**: logic-language/implementation.modal, implementation.deduction.classic, framework.deduction/
- **Used By**: modal-use-case/
- **Java Package**: `com.dan323.proof.structures.deduction.implementation.modal.*`
- **Rule Application**: Includes state label tracking and world relationship validation

### use-cases/

Orchestrates the application logic by combining logic languages and proof structures.

#### model/
- **Purpose**: Domain models and data transfer objects
- **Key Classes**:
  - `Proof` - Proof state and history
  - `ProofStep` - Individual proof step with applied rule
  - `ProofResult` - Result of proof verification
  - `FormulaDto` - Transfer object for formulas
- **Dependencies**: None
- **Used By**: All use cases and REST API
- **Java Package**: `com.dan323.proof.model.*`

#### base-use-case/
- **Purpose**: Common functionality used by both classical and modal use cases
- **Key Components**:
  - Base use case classes
  - Common validation logic
  - Proof serialization utilities
  - Rule discovery and application
- **Dependencies**: logic-language/framework, proof-structures/framework.deduction, model/
- **Used By**: classical-use-case/, modal-use-case/
- **Java Package**: `com.dan323.proof.use_case.base.*`

#### classical-use-case/
- **Purpose**: Application logic for classical propositional logic
- **Key Use Cases**:
  - `VerifyProofUseCase` - Verify a complete proof
  - `GetNextRulesUseCase` - Get applicable rules for current proof state
  - `ApplyRuleUseCase` - Apply a rule to advance proof
  - `ParseFormulaUseCase` - Parse formula strings to objects
- **Dependencies**: logic-language/implementation, proof-structures/implementation.deduction.classic, base-use-case/, model/
- **Used By**: REST API (executable/)
- **Java Package**: `com.dan323.proof.use_case.classical.*`

#### modal-use-case/
- **Purpose**: Application logic for modal propositional logic
- **Key Use Cases**:
  - Same as classical-use-case/ but with modal logic support
  - State/world management
  - Modal operator handling
- **Dependencies**: logic-language/implementation.modal, proof-structures/implementation.deduction.modal, base-use-case/, model/
- **Used By**: REST API (executable/)
- **Java Package**: `com.dan323.proof.use_case.modal.*`

## REST and Executable Modules

### rest/

REST API contracts and models.

#### framework/
- **Purpose**: Generic REST framework utilities
- **Key Components**:
  - Base controller classes
  - Response wrappers
  - Error handling
- **Dependencies**: Spring Framework
- **Used By**: executable/
- **Java Package**: `com.dan323.rest.framework.*`

#### model/
- **Purpose**: REST API models
- **Key Classes**:
  - `ProofRequest` - Input for proof operations
  - `ProofResponse` - Output from proof operations
  - `RuleInfo` - Information about available rules
  - `ErrorResponse` - Standardized error responses
- **Dependencies**: domain/use-cases/model/
- **Used By**: executable/
- **Java Package**: `com.dan323.rest.model.*`

### executable/

The Spring Boot application that ties everything together.

- **Purpose**: Entry point and API server
- **Key Components**:
  - Spring Boot application class
  - REST controllers for classical and modal logic
  - Configuration beans
  - Integration with all domain modules
- **Dependencies**: All domain modules, rest/, Spring Boot
- **Java Package**: `com.dan323.main.*`
- **Endpoints**:
  - `/api/classical/*` - Classical logic endpoints
  - `/api/modal/*` - Modal logic endpoints
  - `/health` - Health check

## Frontend Module

### frontend/

React-based user interface.

- **Purpose**: Web UI for interactive proof building
- **Technology**: React 18, TypeScript, Jest
- **Key Components**:
  - Proof builder interface
  - Formula input component
  - Rule selection and application
  - Proof visualization
  - Goal/assumptions display
- **Location**: `frontend/src/`
- **Build**: `npm run build`
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

