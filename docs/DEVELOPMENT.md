# Development Guide

This guide provides instructions for extending and contributing to the Natural Deduction project.

## Project Structure Recap

For detailed information about modules, see [Project Modules](./MODULES.md).

```
natural-deduction/
├── domain/                 # Core business logic
│   ├── logic-language/    # Formula and operator definitions
│   ├── proof-structures/  # Deduction rules
│   └── use-cases/         # Application orchestration
├── executable/            # Spring Boot application
├── rest/                  # REST API layer
└── frontend/              # React UI
```

## Development Setup

### Prerequisites

- Follow the [Setup & Installation](./SETUP.md) guide
- Familiarity with Java 21, Maven, React, and TypeScript
- Understanding of natural deduction and logic

### Clone and Build

```powershell
git clone https://github.com/dan323/natural-deduction.git
cd natural-deduction
mvn clean install
```

## Architecture Principles

The project follows these key principles:

### 1. **Plugin Architecture**
- **Framework modules** define abstract interfaces
- **Implementation modules** provide concrete implementations
- **New logics** can be added by implementing framework interfaces

### 2. **Separation of Concerns**
- Language layer (formulas) separate from proof layer (rules)
- Use cases orchestrate between layers
- REST layer independent of domain logic

### 3. **Testability**
- Heavy use of unit testing
- Dependency injection for mockability
- Integration tests for use cases

### 4. **Extensibility**
- Easy to add new logical systems
- Easy to add new rules
- Easy to add new REST endpoints

## Adding a New Logical System

To add support for a new logical system (e.g., intuitionistic logic), follow these steps:

### Step 1: Implement Logic Language Framework

Create new module: `domain/logic-language/implementation.intuitionistic/`

```powershell
mkdir domain/logic-language/implementation.intuitionistic/src/main/java/com/dan323/logic/language/implementation/intuitionistic
mkdir domain/logic-language/implementation.intuitionistic/src/test/java/com/dan323/logic/language/implementation/intuitionistic
```

Create `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.dan323</groupId>
        <artifactId>logic-language</artifactId>
        <version>0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>implementation.intuitionistic</artifactId>
    <version>0.1-SNAPSHOT</version>
    
    <dependencies>
        <dependency>
            <groupId>com.dan323</groupId>
            <artifactId>framework</artifactId>
            <version>0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.dan323</groupId>
            <artifactId>implementation</artifactId>
            <version>0.1-SNAPSHOT</version>
        </dependency>
        <!-- test dependencies -->
    </dependencies>
</project>
```

### Step 2: Implement Logic Language Classes

Create intuitionistic-specific term classes:

```java
package com.dan323.logic.language.implementation.intuitionistic;

import com.dan323.logic.language.Term;

public class IntuitionisticTerm implements Term {
    // Implement required methods from Term interface
    // Intuitionistic logic extends classical operators
    // but excludes law of excluded middle: p ∨ ¬p
    
    @Override
    public String toString() {
        // Implementation
    }
}

public class IntuitionisticTermFactory {
    // Create terms from strings
    // Similar to classical implementation but with intuitionistic restrictions
}
```

### Step 3: Implement Proof Structures Framework

Create new module: `domain/proof-structures/implementation.deduction.intuitionistic/`

Implement deduction rules following the classical rules pattern:

```java
package com.dan323.proof.structures.deduction.implementation.intuitionistic;

import com.dan323.proof.structures.deduction.DeductionRule;
import com.dan323.logic.language.Term;

public class IntuitionisticAndIntroductionRule implements DeductionRule {
    @Override
    public boolean canApply(Proof proof, Term premise1, Term premise2) {
        // Check if both premises are proven
    }
    
    @Override
    public Term apply(Proof proof, Term premise1, Term premise2) {
        // Apply the rule
        return new IntuitionisticConjunction(premise1, premise2);
    }
    
    @Override
    public String getName() {
        return "∧-Introduction";
    }
}
```

### Step 4: Create Use Case

Create new module: `domain/use-cases/intuitionistic-use-case/`

```java
package com.dan323.proof.use_case.intuitionistic;

import com.dan323.proof.use_case.base.BaseProofUseCase;
import com.dan323.logic.language.implementation.intuitionistic.*;
import com.dan323.proof.structures.deduction.implementation.intuitionistic.*;

public class IntuitionisticProofUseCase extends BaseProofUseCase {
    // Extend base use case with intuitionistic implementations
    
    public IntuitionisticProofUseCase() {
        super(
            new IntuitionisticTermFactory(),
            new IntuitionisticRuleRegistry()
        );
    }
}
```

### Step 5: Add REST Endpoints

In `executable/src/main/java/com/dan323/executable/controller/`:

```java
package com.dan323.executable.controller;

import org.springframework.web.bind.annotation.*;
import com.dan323.proof.use_case.intuitionistic.*;

@RestController
@RequestMapping("/api/intuitionistic")
public class IntuitionisticController {
    
    private final IntuitionisticProofUseCase proofUseCase;
    
    @PostMapping("/verify")
    public ResponseEntity<ProofResponse> verifyProof(@RequestBody ProofRequest request) {
        // Implementation
    }
    
    // Additional endpoints...
}
```

### Step 6: Update Dependencies

Update `domain/use-cases/pom.xml` to include your use case:

```xml
<module>intuitionistic-use-case</module>
```

Update `executable/pom.xml`:

```xml
<dependency>
    <groupId>com.dan323</groupId>
    <artifactId>intuitionistic-use-case</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

### Step 7: Add Tests

Follow TDD practices and create tests before implementation:

```java
package com.dan323.proof.use_case.intuitionistic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IntuitionisticProofUseCaseTest {
    
    private IntuitionisticProofUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new IntuitionisticProofUseCase();
    }
    
    @Test
    void testParseSimpleFormula() {
        // Test parsing
    }
    
    @Test
    void testApplyIntroductionRule() {
        // Test rule application
    }
}
```

### Step 8: Frontend Support (if needed)

Update frontend to support new logic system in `frontend/src/constant.ts`:

```typescript
export const INTUITIONISTIC_API = `${API_BASE_URL}/api/intuitionistic`;
```

Add UI component for intuitionistic logic in `frontend/src/components/`.

### Step 9: Update Documentation

Update:
- [LANGUAGES.md](./LANGUAGES.md) - Add intuitionistic logic description
- [MODULES.md](./MODULES.md) - Add new modules
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Update if needed

## Common Development Tasks

### Adding a New Rule to an Existing Logic

Example: Add a new rule to classical logic

1. **Create the rule class** in appropriate module:
   ```
   domain/proof-structures/implementation.deduction.classic/src/main/java/com/dan323/proof/structures/deduction/implementation/[RuleName]Rule.java
   ```

2. **Implement the DeductionRule interface**:
   ```java
   public class CustomRule implements DeductionRule {
       @Override
       public boolean canApply(Proof proof, Term... premises) { ... }
       
       @Override
       public Term apply(Proof proof, Term... premises) { ... }
       
       @Override
       public String getName() { ... }
   }
   ```

3. **Register the rule** in the rule registry or factory

4. **Add unit tests**:
   ```
   domain/proof-structures/implementation.deduction.classic/src/test/java/com/dan323/proof/structures/deduction/implementation/[RuleName]RuleTest.java
   ```

5. **Update use case** if needed

### Modifying an Existing Rule

1. Update the rule implementation
2. Update all tests that use this rule
3. Run full test suite: `mvn clean test`
4. Update documentation if rule behavior changes

### Extending the REST API

1. **Create a new controller** in `executable/src/main/java/com/dan323/executable/controller/`
2. **Add request/response models** in `rest/model/`
3. **Implement handler methods** that call use cases
4. **Add tests** in `executable/src/test/java/`
5. **Document endpoints** in [API.md](./API.md)

### Frontend Component Development

1. **Create component** in `frontend/src/components/`
2. **Add component styles** in `frontend/src/components/[Component].css`
3. **Write tests** in `frontend/src/components/[Component].test.tsx`
4. **Export from index** if creating a new feature directory
5. **Integrate** into main App.tsx or parent component

Example component:

```typescript
import React from 'react';
import './ProofVerifier.css';

interface ProofVerifierProps {
    proof: Proof;
    onRuleApply: (rule: string) => void;
}

export const ProofVerifier: React.FC<ProofVerifierProps> = ({ 
    proof, 
    onRuleApply 
}) => {
    return (
        <div className="proof-verifier">
            {/* Component content */}
        </div>
    );
};
```

## Testing Best Practices

### Unit Testing

Use JUnit 5 and Mockito:

```java
public class ClassicalAndIntroductionRuleTest {
    
    private ClassicalAndIntroductionRule rule;
    private Proof mockProof;
    
    @BeforeEach
    void setUp() {
        rule = new ClassicalAndIntroductionRule();
        mockProof = mock(Proof.class);
    }
    
    @Test
    void canApply_withBothPremises_returnsTrue() {
        // Arrange
        Term premise1 = createTerm("p");
        Term premise2 = createTerm("q");
        when(mockProof.contains(premise1)).thenReturn(true);
        when(mockProof.contains(premise2)).thenReturn(true);
        
        // Act
        boolean result = rule.canApply(mockProof, premise1, premise2);
        
        // Assert
        assertTrue(result);
    }
}
```

### Integration Testing

Test use cases end-to-end:

```java
public class ClassicalProofUseCaseIntegrationTest {
    
    private ClassicalProofUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new ClassicalProofUseCase();
    }
    
    @Test
    void endToEnd_simpleProof() {
        // Test complete proof workflow
    }
}
```

### Frontend Testing

Use Jest and React Testing Library:

```typescript
import { render, screen } from '@testing-library/react';
import { ProofVerifier } from './ProofVerifier';

describe('ProofVerifier', () => {
    it('renders proof correctly', () => {
        const mockProof = { /* ... */ };
        render(<ProofVerifier proof={mockProof} onRuleApply={jest.fn()} />);
        
        expect(screen.getByText(/proof/i)).toBeInTheDocument();
    });
});
```

## Code Quality

### Running Code Coverage

```powershell
# Backend coverage
mvn clean install
# Open: jacoco-natural-deduction/target/site/jacoco/index.html

# Frontend coverage
cd frontend
npm test -- --coverage
# View: coverage/lcov-report/index.html
```

### Code Style

- Follow Java naming conventions
- Use meaningful variable names
- Keep methods focused (single responsibility)
- Add Javadoc for public APIs
- Max line length: 120 characters

### Static Analysis

```powershell
# Run local SonarQube analysis (if configured)
mvn clean install sonar:sonar
```

## Commit Guidelines

Follow conventional commits:

```
feat: add new rule type
fix: correct proof validation logic
docs: update architecture documentation
refactor: simplify term representation
test: add tests for modal logic
chore: update dependencies
```

Example:
```
feat(modal-logic): implement box elimination rule

- Add □-Elimination rule for modal logic
- Update modal use case with new rule
- Add comprehensive tests
- Update documentation

Closes #123
```

## Pull Request Process

1. Create a feature branch: `git checkout -b feat/your-feature`
2. Make changes and commit with meaningful messages
3. Ensure tests pass: `mvn clean test`
4. Ensure code quality: Check SonarCloud results
5. Push to GitHub and create a Pull Request
6. Address any review comments
7. Merge after approval

## Performance Considerations

- Use caching for expensive formula parsing operations
- Profile proof checking with large formulas
- Monitor memory usage in modal logic (multiple worlds)
- Use appropriate data structures (TreeMap for ordered rules, etc.)

## Debugging Tips

### Backend Debugging

In IntelliJ IDEA:
1. Set breakpoints in code
2. Run → Debug 'Application'
3. Use debugger tools to inspect variables

In VS Code:
1. Install "Debugger for Java"
2. Create launch configuration
3. Run → Start Debugging

### Frontend Debugging

Use browser DevTools:
1. Chrome/Firefox DevTools (F12)
2. Console tab for logs
3. Network tab for API calls
4. React DevTools extension

## Documentation Standards

When updating documentation:
- Use clear, concise language
- Include examples where applicable
- Link to related documents
- Update table of contents if needed
- Follow Markdown conventions

## Need Help?

- Check existing [issues](https://github.com/dan323/natural-deduction/issues)
- Review existing code in similar modules
- Ask questions in issue discussions
- Reference [Architecture Overview](./ARCHITECTURE.md)

## References

- [Setup & Installation](./SETUP.md) - Environment setup
- [Architecture Overview](./ARCHITECTURE.md) - System design
- [Project Modules](./MODULES.md) - Module documentation
- [Logical Languages](./LANGUAGES.md) - Logic specifications
- [API Reference](./API.md) - REST API documentation

