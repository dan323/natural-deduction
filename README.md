# Natural Deduction

A system for [natural deduction](https://en.wikipedia.org/wiki/Natural_deduction) supporting classical, intuitionistic and modal propositional logic, with
exercises and an automatic solver.

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

The jar serves the REST API (`/logic/{logic}/...`, see [docs/API.md](./docs/API.md)). It only serves the web UI if the
frontend was built into it first (see [Setup & Installation](./docs/SETUP.md)); the published Docker image
(`dan323/natural-deduction`) always includes it:

```bash
docker run -p 8080:8080 dan323/natural-deduction
```

## Supported Logics

- **Classical Propositional Logic** (`classical`) - Standard propositional calculus with natural deduction rules
- **Intuitionistic Propositional Logic** (`intuitionistic`) - Classical logic without double negation elimination
- **Modal Propositional Logic** (`modal`) - Extends classical logic with modal operators (necessity □, possibility ◇)
  over states reachable by a reflexive and transitive relation
- **Modal Logic with Next and Until** (`modal-next-until`) - Modal logic over discrete time, with Next `X` and Until `U`

Every logic has a catalog of exercises; classical and modal logic also have an automatic solver.

## Documentation

Complete documentation is available in the [docs/](./docs/) folder:

- **[Overview & Getting Started](./docs/README.md)** - Project overview and features
- **[Setup & Installation](./docs/SETUP.md)** - Build and run instructions
- **[Architecture](./docs/ARCHITECTURE.md)** - System design and component structure
- **[Modules](./docs/MODULES.md)** - Detailed module descriptions
- **[Logical Languages](./docs/LANGUAGES.md)** - The logics and their rules
- **[Development Guide](./docs/DEVELOPMENT.md)** - Contributing and extending
- **[REST API](./docs/API.md)** - API endpoint reference

## Technology Stack

- **Backend**: Java 21, Spring Boot 4.1.1
- **Frontend**: React 19, TypeScript, Vite
- **Build**: Maven, Docker
- **Testing**: JUnit 6, Jest, PIT mutation testing
- **Quality**: SonarCloud, JaCoCo

## License

Copyright (C) 2019-2026 dan323

Licensed under the GNU General Public License, version 3 only (`GPL-3.0-only`). See [LICENSE](./LICENSE) for details.
