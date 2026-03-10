# Documentation Index

## Getting Started

New to the project? Start here:

1. **[README.md](../README.md)** - Project overview and quick start
2. **[docs/README.md](./README.md)** - Comprehensive introduction to the Natural Deduction system
3. **[docs/SETUP.md](./SETUP.md)** - Installation, configuration, and running the application
4. **[docs/QUICK-REFERENCE.md](./QUICK-REFERENCE.md)** - Common commands and workflows

## Understanding the Project

Learn about the architecture and design:

1. **[docs/ARCHITECTURE.md](./ARCHITECTURE.md)** - System design, layered architecture, and plugin structure
2. **[docs/MODULES.md](./MODULES.md)** - Detailed descriptions of each module and their dependencies
3. **[docs/LANGUAGES.md](./LANGUAGES.md)** - Specifications for classical and modal propositional logic

## Development

Contribute to the project:

1. **[docs/DEVELOPMENT.md](./DEVELOPMENT.md)** - Contributing guidelines, adding new features, and best practices
2. **[docs/API.md](./API.md)** - REST API endpoints and request/response formats

## Quick Reference

### File Navigation Map

```
natural-deduction/
├── README.md                    [Start here - overview]
├── docs/
│   ├── README.md               [Complete introduction]
│   ├── SETUP.md                [Installation & running]
│   ├── ARCHITECTURE.md         [System design]
│   ├── MODULES.md              [Module reference]
│   ├── LANGUAGES.md            [Logic specifications]
│   ├── DEVELOPMENT.md          [Contributing]
│   ├── API.md                  [REST API reference]
│   ├── QUICK-REFERENCE.md      [Common commands & workflows]
│   └── INDEX.md                [This file]
├── domain/                      [Core business logic]
├── executable/                  [Spring Boot application]
├── rest/                        [REST API layer]
└── frontend/                    [React UI]
```

### By Use Case

**I want to...**

| Goal                               | Document                                   |
|------------------------------------|--------------------------------------------|
| Understand what this project does  | [README.md](../README.md)                  |
| Get the project running            | [SETUP.md](./SETUP.md)                     |
| Quick build/run commands           | [QUICK-REFERENCE.md](./QUICK-REFERENCE.md) |
| Understand the system architecture | [ARCHITECTURE.md](./ARCHITECTURE.md)       |
| Learn about modules                | [MODULES.md](./MODULES.md)                 |
| Understand the logic systems       | [LANGUAGES.md](./LANGUAGES.md)             |
| Add a new feature                  | [DEVELOPMENT.md](./DEVELOPMENT.md)         |
| Use the REST API                   | [API.md](./API.md)                         |
| Contribute code                    | [DEVELOPMENT.md](./DEVELOPMENT.md)         |

## Document Relationships

```
README.md (root)
  ↓
docs/README.md
  ├─→ SETUP.md [How to get it running]
  │   └─→ Application configuration
  │
  ├─→ ARCHITECTURE.md [How it works]
  │   ├─→ MODULES.md [What are the components]
  │   │   └─→ Specific module pom.xml files
  │   │
  │   └─→ Component interaction flow
  │
  ├─→ LANGUAGES.md [What logic systems are supported]
  │   ├─→ Classical propositional logic rules
  │   └─→ Modal propositional logic rules
  │
  └─→ DEVELOPMENT.md [How to extend it]
      ├─→ Adding new logical systems
      ├─→ Adding new rules
      ├─→ Contributing guidelines
      └─→ Testing best practices
  
  └─→ API.md [How to use the REST API]
      ├─→ Classical endpoints
      ├─→ Modal endpoints
      └─→ System endpoints
```

## Core Concepts

### Plugin-Based Architecture
- Framework modules define contracts
- Implementation modules provide concrete logic
- Easy to add new logical systems
- See: [ARCHITECTURE.md](./ARCHITECTURE.md)

### Separation of Concerns
- **Logic Language Layer**: How formulas are represented
- **Proof Structures Layer**: How rules are applied
- **Use Cases Layer**: Application orchestration
- **REST/Frontend**: User interface
- See: [ARCHITECTURE.md](./ARCHITECTURE.md) and [MODULES.md](./MODULES.md)

### Multiple Logic Systems
- **Classical Logic**: Standard propositional calculus
- **Modal Logic**: Extends classical with □ (necessity) and ◇ (possibility)
- See: [LANGUAGES.md](./LANGUAGES.md)

### Natural Deduction Rules
- Introduction and elimination rules for each operator
- Proper handling of assumptions and discharges
- World/state management for modal logic
- See: [LANGUAGES.md](./LANGUAGES.md) and [API.md](./API.md)

## Technical Stack

| Layer    | Technology                 | Documentation                      |
|----------|----------------------------|------------------------------------|
| Backend  | Java 21, Spring Boot 3.3.2 | [SETUP.md](./SETUP.md)             |
| Frontend | React 18, TypeScript       | [SETUP.md](./SETUP.md)             |
| Build    | Maven                      | [SETUP.md](./SETUP.md)             |
| Testing  | JUnit 5, Jest, PIT         | [SETUP.md](./SETUP.md)             |
| Quality  | SonarCloud, JaCoCo         | [DEVELOPMENT.md](./DEVELOPMENT.md) |

## Frequently Asked Questions

**Q: How do I get started with development?**
A: See [SETUP.md](./SETUP.md) for installation, then [DEVELOPMENT.md](./DEVELOPMENT.md) for contribution guidelines.

**Q: What's the difference between classical and modal logic?**
A: See [LANGUAGES.md](./LANGUAGES.md) for detailed specifications of both systems.

**Q: How do I add support for a new logic system?**
A: See [DEVELOPMENT.md](./DEVELOPMENT.md) section "Adding a New Logical System".

**Q: How does the REST API work?**
A: See [API.md](./API.md) for complete endpoint reference and examples.

**Q: How are the modules organized?**
A: See [MODULES.md](./MODULES.md) for module hierarchy and dependencies.

**Q: What's the architecture?**
A: See [ARCHITECTURE.md](./ARCHITECTURE.md) for system design and component relationships.

## Document Format

All documents use standard Markdown with:
- Clear section headings (H2-H4)
- Code blocks with syntax highlighting
- Internal links to related documents
- Tables for reference information
- ASCII diagrams for complex concepts

## Contributing to Documentation

When updating documentation:
1. Keep language clear and concise
2. Include code examples where applicable
3. Link to related documents
4. Update this INDEX.md if adding new documents
5. Use consistent formatting

## External References

- [Natural Deduction on Wikipedia](https://en.wikipedia.org/wiki/Natural_deduction)
- [Propositional Calculus](https://en.wikipedia.org/wiki/Propositional_calculus)
- [Modal Logic](https://en.wikipedia.org/wiki/Modal_logic)
- [Kripke Semantics](https://en.wikipedia.org/wiki/Kripke_semantics)
- [GitHub Repository](https://github.com/dan323/natural-deduction)

## Last Updated

This documentation was created on 2024-01-15. Refer to git history for version changes.

