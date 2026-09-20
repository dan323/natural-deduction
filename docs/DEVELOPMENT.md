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

### Step 1: Create the Modules

Copy the layout of the existing logics (for modal: `domain/logic-language/implementation.modal`,
`domain/proof-structures/implementation.deduction.modal`, `domain/use-cases/modal-use-case`) and list the new modules
in the `<modules>` of their parent pom. A module's parent is its group's pom (`logic-language`, `proof-structures`,
`use-cases`), it depends on the modules it builds on (e.g. `language-framework`) and it has a `module-info.java`.

### Steps 2-5: What a Logic Has to Provide

The code of `classical` and `modal` is the reference; in short:

1. **Formulas** (`domain/logic-language/`): subclass the operators of `framework` (`Conjunction`, `Implication`, ...)
   and write a parser (the existing ones are built on javaluator).
2. **Proofs** (`domain/proof-structures/`): bind `framework.deduction` (`Proof<T,Q>`, `ProofStep`, `Action`, the generic
   rule bases) to the language: a proof class (`NaturalDeduction`, `ModalNaturalDeduction`), one class per rule,
   a `Parse*Action` to build rules from their names and reasons, and the automatic solver (`automate()`).
3. **Use case** (`domain/use-cases/<logic>-use-case`): expose three Spring beans, keyed by the logic name (`"classical"`,
   `"modal"`): a `Transformer` (DTO to domain proof and back, by replaying every step), a `ProofParser` (text file to
   proof) and a `LogicalGetActions` (the descriptors of the actions, see `ActionDescriptorDto`). Add a `*Configuration`
   that declares them.
4. **Wire it** in `executable/.../ApplicationConfiguration` by importing that configuration. No new controller is
   needed: `ControllerInterface` serves every logic under `/logic/{logic}/...`.

Every module has a `module-info.java`: packages that another module needs must be `exports`ed and new dependencies
need a `requires`.

### Step 6: Update Dependencies

Add the use-case module to `<modules>` in `domain/use-cases/pom.xml`, and depend on it from `executable/pom.xml`
(next to `classical-use-case` and `modal-use-case`), so that it is on the jar's classpath.

### Step 7: Add Tests

Test each layer as the existing logics do (see `ClassicAndTest` in `implementation.deduction.classic`, the tests of
`modal-use-case`) and add the new logic to the integration tests in `executable/src/test/java`. `RestServiceIT` checks
the `/logic/{logic}/actions|action|solve` endpoints of `classical` and `modal`, and `FatJarActionsIT` checks the
action lists from the packaged jar. If the rule list of the new logic is an enum, add a test that every entry builds
an action, as `modal-use-case` does for `AvailableModalAction`.

### Step 8: Frontend Support (if needed)

The UI calls relative URLs and is hardcoded to one logic: `LOGIC` in `frontend/src/constant.ts` (`"classical"`; modal
is backend only). Make it selectable there if the new logic should be usable from the UI.

### Step 9: Update Documentation

Update:
- [LANGUAGES.md](./LANGUAGES.md) - Add intuitionistic logic description
- [MODULES.md](./MODULES.md) - Add new modules
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Update if needed

## Common Development Tasks

### Adding a New Rule to an Existing Logic

Example: add a new rule to classical logic

1. **Create the rule class** `Classic<Name>` in `domain/proof-structures/implementation.deduction.classic`
   (package `com.dan323.classical`), extending the generic base of `framework.deduction` if there is one (`AndI`,
   `ModusPonens`, ...) and implementing `ClassicalAction`. A rule checks itself with `isValid(proof)` and adds its step
   with `apply(proof)`.
2. **Add it to `AvailableAction`**, to `ParseClassicalAction` (so that it can be built from its name, and parsed from a
   proof file) and to the `switch` in `ClassicGetActions` in `classical-use-case`, which lists its inputs (`INT`,
   `EXPRESSION`). That `switch` has no default branch, so the build fails until it is described. For modal logic add a
   value to `AvailableModalAction` and to `ParseModalAction`.
3. **Add unit tests** next to the existing ones in `src/test/java/com/dan323/proof/classic/`
4. **Update the automatic solver** (`ClassicalAutomate`) if it should use the rule

### Modifying an Existing Rule

1. Update the rule implementation
2. Update all tests that use this rule
3. Run the full test suite: `mvn clean verify`
4. Update documentation if rule behavior changes

### Extending the REST API

1. **Add the handler** to `ControllerInterface` in `rest/framework/` (it serves every logic under `/logic/{logic}`)
2. **Add request/response models** in `rest/model/`
3. **Call the use cases** of `ActionsUseCases`; map failures to a status in `RestExceptionHandler`
4. **Add tests** in `executable/src/test/java/` (`RestServiceIT`, run by `mvn verify`)
5. **Document endpoints** in [API.md](./API.md)

### Frontend Component Development

1. **Create the component** in `frontend/src/components/<feature>/`, with its styles next to it
2. **Write tests** in the `__test__/` folder of that feature (Jest + React Testing Library)
3. **Integrate** it into `App.tsx` or a parent component
4. Calls to the backend go through `frontend/src/service/actions.ts`; types shared with the API are in `src/types.d.ts`
5. Run `npm run typecheck` and `npm test`

## Testing Best Practices

### Unit Testing

Use JUnit 6 and Mockito. Rules are tested against real proofs (see `ClassicAndTest`): build a proof, check
`isValid(proof)`, `apply(proof)` and assert on the steps.

### Integration Testing

`*IT.java` classes in `executable/src/test/java` run in the `verify` phase (a second surefire execution), so plain
`mvn test` skips them:

- `RestServiceIT` - the REST API on a random port
- `RestSolveTimeoutIT` - the 422 answer of a solve that does not finish, with a test-only logic
- `FatJarActionsIT` - starts the packaged fat jar and checks the action lists and the solver; only works through `mvn verify`

`SpringVersionAlignmentTest` (a unit test) fails when `spring.version` in the root pom no longer matches the Spring
version managed by the Spring Boot parent of `executable`; bump both together.

### Frontend Testing

Use Jest and React Testing Library. Tests live in `__test__/` folders next to the code (e.g.
`frontend/src/components/menu/__test__/`). Run one with `npx jest src/components/menu`.

## Code Quality

### Running Code Coverage

```powershell
# Backend coverage
mvn clean install
# Open: jacoco-natural-deduction/target/site/jacoco/index.html

# Frontend coverage
cd frontend
npm test
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

Commit and PR titles are short imperative sentences, with the PR number when merged, as in the git history
(e.g. "Fix the minor items of the project review (#101)").

## Pull Request Process

1. Create a feature branch: `git checkout -b feat/your-feature`
2. Make changes and commit with meaningful messages
3. Ensure the checks pass locally: `mvn -B verify`, and in `frontend/` `npm run typecheck` and `npm test`
4. Check the CI results (tests, SonarCloud, mutation testing)
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

