# Natural Deduction - Documentation

Welcome to the Natural Deduction project documentation. This project implements a system for [natural deduction](https://en.wikipedia.org/wiki/Natural_deduction) with support for classical, intuitionistic and modal propositional logic.

## Quick Navigation

- [Architecture Overview](./ARCHITECTURE.md) - System design and component relationships
- [Project Modules](./MODULES.md) - Detailed description of each module
- [Setup & Installation](./SETUP.md) - How to build and run the project
- [Development Guide](./DEVELOPMENT.md) - Contributing and extending the system
- [API Reference](./API.md) - REST API documentation
- [Logical Languages](./LANGUAGES.md) - The logics and their rules

## Project Status

[![Main Workflow](https://github.com/dan323/natural-deduction/actions/workflows/CompileAndTest.yml/badge.svg)](https://github.com/dan323/natural-deduction/actions/workflows/CompileAndTest.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=natural-deduction&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=natural-deduction)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=natural-deduction&metric=coverage)](https://sonarcloud.io/summary/new_code?id=natural-deduction)

## What is Natural Deduction?

Natural deduction is a type of proof system that uses rules of inference to establish logical consequences from premises. Unlike axiomatic systems, natural deduction mimics the way mathematicians and logicians naturally reason.

### Supported Logical Systems

This project supports four logics, each served under its own name (`/logic/{logic}/...`):

1. **Classical Propositional Logic** (`classical`) - Standard propositional calculus with classical rules
2. **Intuitionistic Propositional Logic** (`intuitionistic`) - Classical logic without double negation elimination
3. **Modal Propositional Logic** (`modal`) - Extends classical logic with modal operators (necessity, possibility) using labeled states
4. **Modal Logic with Next and Until** (`modal-next-until`) - Modal logic over discrete time, with Next and Until

## Getting Started

For detailed setup instructions, see [Setup & Installation](./SETUP.md).

### Quick Start

```bash
# Build the project
mvn clean install

# Run the executable
java -jar executable/target/executable-0.1-SNAPSHOT.jar

# Start the frontend dev server (proxies /logic to the backend on :8080)
cd frontend
npm ci
npm start
```

## Project Structure

The project is organized as follows:

- **domain/** - Core business logic and domain models
  - `logic-language/` - Logical language definitions
  - `proof-structures/` - Proof system implementations
  - `use-cases/` - Application use cases
- **executable/** - Spring Boot application serving the REST API
- **frontend/** - React-based user interface
- **rest/** - REST API framework and models

See [Project Modules](./MODULES.md) for more details.

## Key Features

- 🧠 **Pluggable Logic Systems** - Easily add support for new logical systems
- 🏗️ **Framework-based Architecture** - Separation between framework and implementations
- 📊 **Natural Deduction Rules** - Comprehensive rule implementations for every logic
- 🌐 **REST API** - HTTP API to list the rules and the exercises, apply a rule, upload a proof and run the automatic solver
- 🎓 **Exercises** - A catalog of exercises per logic, from easy to hard, with the solved ones remembered in the browser
- 💻 **Web UI** - Interactive interface for building and verifying proofs
- ✅ **Comprehensive Tests** - High code coverage with unit and integration tests

## Technology Stack

- **Backend**: Java 21, Spring Boot 4.1.1, Maven
- **Frontend**: React 19, TypeScript, Vite, Jest
- **Testing**: JUnit 6, Mockito, PIT mutation testing
- **Quality**: SonarCloud, JaCoCo coverage
- **Deployment**: Docker (non-root image with a health check, published to Docker Hub by CI after the tests and a smoke test pass)

## Contributing

Please see [Development Guide](./DEVELOPMENT.md) for guidelines on contributing to this project.

## License

See the [LICENSE](../LICENSE) file for licensing information.

