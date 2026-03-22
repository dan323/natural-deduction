# Natural Deduction

A system for [natural deduction](https://en.wikipedia.org/wiki/Natural_deduction) supporting classical and modal propositional logic.

## Status

[![Main Workflow](https://github.com/dan323/natural-deduction/actions/workflows/CompileAndTest.yml/badge.svg)](https://github.com/dan323/natural-deduction/actions/workflows/CompileAndTest.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=natural-deduction&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=natural-deduction)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=natural-deduction&metric=bugs)](https://sonarcloud.io/summary/new_code?id=natural-deduction)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=natural-deduction&metric=coverage)](https://sonarcloud.io/summary/new_code?id=natural-deduction)

## Quick Start

```bash
# Build the project
mvn clean install

# Run the application
java -jar executable/target/executable-0.1-SNAPSHOT.jar

# Access at http://localhost:8080
```

## Supported Logics

- **Classical Propositional Logic** - Standard propositional calculus with natural deduction rules
- **Modal Propositional Logic** - Extends classical logic with modal operators (necessity □, possibility ◇)

## Documentation

Complete documentation is available in the [docs/](./docs/) folder:

- **[Overview & Getting Started](./docs/README.md)** - Project overview and features
- **[Setup & Installation](./docs/SETUP.md)** - Build and run instructions
- **[Architecture](./docs/ARCHITECTURE.md)** - System design and component structure
- **[Modules](./docs/MODULES.md)** - Detailed module descriptions
- **[Logical Languages](./docs/LANGUAGES.md)** - Classical and modal logic specifications
- **[Development Guide](./docs/DEVELOPMENT.md)** - Contributing and extending
- **[REST API](./docs/API.md)** - API endpoint reference

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.2
- **Frontend**: React 18, TypeScript
- **Build**: Maven
- **Testing**: JUnit 5, Jest, PIT mutation testing
- **Quality**: SonarCloud, JaCoCo

## License

See [LICENSE](./LICENSE) for details.
