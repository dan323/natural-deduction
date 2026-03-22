# Quick Reference

Common commands and workflows for the Natural Deduction project.

## Project Structure

```
natural-deduction/
├── docs/                          # Documentation (THIS IS NEW!)
│   ├── README.md                  # Documentation overview
│   ├── SETUP.md                   # Installation & running
│   ├── ARCHITECTURE.md            # System design
│   ├── MODULES.md                 # Module reference
│   ├── LANGUAGES.md               # Logic specifications
│   ├── DEVELOPMENT.md             # Development guide
│   ├── API.md                     # REST API reference
│   ├── INDEX.md                   # Navigation guide
│   └── QUICK-REFERENCE.md         # This file
├── domain/                        # Core business logic
│   ├── logic-language/            # Formula definitions
│   ├── proof-structures/          # Deduction rules
│   └── use-cases/                 # Use cases
├── executable/                    # Spring Boot application
├── rest/                          # REST API layer
├── frontend/                      # React UI
└── README.md                      # Project overview (updated!)
```

## Build Commands

### Full Build
```powershell
mvn clean install
```

### Build Specific Module
```powershell
mvn clean install -pl domain/logic-language/framework
```

### Skip Tests
```powershell
mvn clean install -DskipTests
```

### Build and Dependencies
```powershell
mvn clean install -amd -pl domain/logic-language/framework
```

## Running the Application

### Start Backend Only
```powershell
java -jar executable/target/executable-0.1-SNAPSHOT.jar
```

### Start with Maven
```powershell
mvn spring-boot:run -pl executable
```

### Start Frontend Development Server
```powershell
cd frontend
npm start
```

### Full Development Setup

Terminal 1 - Backend:
```powershell
mvn spring-boot:run -pl executable
```

Terminal 2 - Frontend:
```powershell
cd frontend
npm start
```

## Testing

### Run All Tests
```powershell
mvn clean test
```

### Run Specific Module Tests
```powershell
mvn test -pl domain/logic-language/framework
```

### Frontend Tests
```powershell
cd frontend
npm test
```

### Frontend Tests with Coverage
```powershell
cd frontend
npm test -- --coverage
```

### JaCoCo Coverage Report
```powershell
mvn clean install
# Open: jacoco-natural-deduction/target/site/jacoco/index.html
```

## Docker

### Build Image
```powershell
docker build -t natural-deduction:latest .
```

### Run Container
```powershell
docker run -p 8080:8080 natural-deduction:latest
```

## API Examples

### Parse Classical Formula
```powershell
$body = @{ formula = "p & q" } | ConvertTo-Json
curl -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/classical/parse
```

### Parse Modal Formula
```powershell
$body = @{ formula = "[]p -> <>q" } | ConvertTo-Json
curl -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/modal/parse
```

### Get Available Rules
```powershell
$body = @{ 
    currentGoal = "p | q"
} | ConvertTo-Json
curl -X POST `
  -H "Content-Type: application/json" `
  -d $body `
  http://localhost:8080/api/classical/rules
```

### Health Check
```powershell
curl http://localhost:8080/actuator/health
```

## Development Workflow

### Setup Development Environment
```powershell
git clone https://github.com/dan323/natural-deduction.git
cd natural-deduction
mvn clean install
cd frontend
npm install
```

### Make Changes
1. Edit code in appropriate module
2. Run tests: `mvn test -pl [module]`
3. Run full test suite: `mvn clean test`
4. Check code quality

### Before Committing
```powershell
# Run full build and tests
mvn clean install

# Run frontend tests
cd frontend
npm test

# Check code coverage
# Review SonarCloud report
```

## Troubleshooting

### Port Already in Use
```powershell
# Find process using port 8080
Get-NetTCPConnection -LocalPort 8080

# Kill process
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process
```

### Clear Node Modules
```powershell
cd frontend
rm -r node_modules package-lock.json
npm install
```

### Clear Maven Cache
```powershell
mvn clean -DskipTests install
```

### Rebuild Frontend Assets
```powershell
cd frontend
npm run build
mvn clean install -pl executable
```

## Key Directories

| Directory | Purpose |
|-----------|---------|
| `domain/logic-language/` | Formula and operator definitions |
| `domain/proof-structures/` | Deduction rules and proof checking |
| `domain/use-cases/` | Application orchestration |
| `executable/` | Spring Boot application and REST controllers |
| `rest/` | REST API layer and models |
| `frontend/` | React UI |
| `docs/` | Complete documentation |

## Important Files

| File | Purpose |
|------|---------|
| `pom.xml` (root) | Maven parent configuration |
| `executable/pom.xml` | Spring Boot dependencies |
| `frontend/package.json` | Node.js dependencies |
| `frontend/src/constant.ts` | API endpoint configuration |
| `executable/src/main/resources/application.properties` | Spring Boot config |

## Useful Links

| Link | Purpose |
|------|---------|
| http://localhost:8080 | Application (when running) |
| http://localhost:3000 | Frontend dev server (when running) |
| http://localhost:8080/actuator/health | Health check |
| http://localhost:8080/api/classical | Classical logic API |
| http://localhost:8080/api/modal | Modal logic API |
| [GitHub](https://github.com/dan323/natural-deduction) | Repository |
| [SonarCloud](https://sonarcloud.io/project/overview?id=dan323_natural-deduction) | Code quality |

## Documentation Files

- **[docs/README.md](./README.md)** - Start here for comprehensive introduction
- **[docs/SETUP.md](./SETUP.md)** - Installation and running guide
- **[docs/ARCHITECTURE.md](./ARCHITECTURE.md)** - System design details
- **[docs/MODULES.md](./MODULES.md)** - Module descriptions
- **[docs/LANGUAGES.md](./LANGUAGES.md)** - Logic specifications
- **[docs/DEVELOPMENT.md](./DEVELOPMENT.md)** - Contributing guide
- **[docs/API.md](./API.md)** - REST API reference
- **[docs/INDEX.md](./INDEX.md)** - Documentation navigation

## IDE Setup

### IntelliJ IDEA
1. Open project
2. Maven auto-configures
3. Run → Edit Configurations → Add Spring Boot Application
4. Main class: `com.dan323.main.Application`

### VS Code
1. Install "Extension Pack for Java"
2. Install "Spring Boot Extension Pack"
3. Create `.vscode/launch.json` with Spring Boot config

## Environment Variables

No specific environment variables required. Optional:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:MAVEN_HOME = "C:\maven"
$env:NODE_HOME = "C:\nodejs"
```

## Version Info

- **Java**: 21
- **Spring Boot**: 3.3.2
- **Maven**: 3.6.0+
- **Node.js**: 16.x+
- **React**: 18.3.1
- **TypeScript**: 4.9.5

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Tests fail after pulling | `mvn clean install` |
| Port 8080 in use | Kill process or change port |
| Frontend can't connect | Ensure backend running on 8080 |
| npm install errors | Delete node_modules and package-lock.json |
| Maven build fails | Check Java version is 21+ |

## Performance Tips

1. Use `-DskipTests` during development if tests are slow
2. Use IDE debugger for profiling
3. Monitor memory with `jcmd`
4. Use `mvn verify` for integration tests only
5. Cache Maven dependencies locally

## Getting Help

1. Check [docs/INDEX.md](./INDEX.md) for navigation
2. Review [docs/DEVELOPMENT.md](./DEVELOPMENT.md)
3. Check existing [GitHub Issues](https://github.com/dan323/natural-deduction/issues)
4. Review code in similar modules
5. Check test files for examples

## References

See full documentation in [docs/](../docs/) folder.

- [Architecture Overview](./ARCHITECTURE.md)
- [Project Modules](./MODULES.md)
- [Logical Languages](./LANGUAGES.md)
- [Development Guide](./DEVELOPMENT.md)
- [API Reference](./API.md)

---

**Last Updated**: 2024-01-15

