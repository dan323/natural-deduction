# Quick Reference

Common commands and workflows for the Natural Deduction project.

## Project Structure

```
natural-deduction/
├── docs/                          # Documentation
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
└── README.md                      # Project overview
```

## Build Commands

### Full Build
```powershell
mvn clean install     # unit tests + *IT.java integration tests, incl. one that boots the packaged jar
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
npm start             # proxies /logic to the backend on :8080, so start the backend first
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
mvn clean verify      # `test` alone skips the *IT.java integration tests
```

### Run Specific Module Tests
```powershell
mvn test -pl domain/logic-language/framework
```

### Frontend Type Check and Tests
```powershell
cd frontend
npm run typecheck
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

### Get the Available Actions
```powershell
curl http://localhost:8080/logic/classical/actions
curl http://localhost:8080/logic/intuitionistic/actions
curl http://localhost:8080/logic/modal/actions
curl http://localhost:8080/logic/modal-next-until/actions
```

### List the Exercises
```powershell
curl http://localhost:8080/logic/classical/exercises
```

### Solve a Proof Automatically
```powershell
curl -X POST -H "Content-Type: application/json" `
  -d '{\"logic\":\"classical\",\"goal\":\"P -> P\",\"steps\":[]}' `
  http://localhost:8080/logic/classical/solve
```

The body is a `ProofDto` (`logic`, `goal`, `steps`). See [API.md](./API.md) for `POST /logic/{logic}/action` and
`POST /logic/{logic}/proof`.

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
npm ci
```

### Make Changes
1. Edit code in appropriate module
2. Run tests: `mvn test -pl [module]`
3. Run full test suite: `mvn clean verify`
4. Check code quality

### Before Committing
```powershell
# Run full build and tests
mvn clean verify

# Type check and run frontend tests
cd frontend
npm run typecheck
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
rm -r node_modules
npm ci
```

### Clean Rebuild Without Tests
```powershell
mvn clean -DskipTests install
```

### Rebuild Frontend Assets
```powershell
cd frontend
npm run build
New-Item -ItemType Directory -Force ../executable/src/main/resources/public | Out-Null
Remove-Item -Recurse -Force ../executable/src/main/resources/public/*   # drop the files of an earlier build
cp -r build/* ../executable/src/main/resources/public/
cd ..
mvn clean install
```

## Key Directories

| Directory | Purpose |
|-----------|---------|
| `domain/logic-language/` | Formula and operator definitions |
| `domain/proof-structures/` | Deduction rules and proof checking |
| `domain/use-cases/` | Application orchestration |
| `executable/` | Spring Boot application (serves the API and the embedded UI) |
| `rest/` | REST controller (`ControllerInterface`), error handling and models |
| `frontend/` | React UI |
| `docs/` | Complete documentation |

## Important Files

| File | Purpose |
|------|---------|
| `pom.xml` (root) | Maven parent configuration |
| `executable/pom.xml` | Spring Boot dependencies |
| `frontend/package.json` | Node.js dependencies |
| `frontend/src/constant.ts` | The logics the UI offers (`LOGICS`) |
| `frontend/vite.config.ts` | Vite config, incl. the `/logic` dev proxy |
| `Dockerfile` | Image of the jar (non-root, health check) |
| `.github/workflows/` | CI: `CompileAndTest.yml`, `frontend.yml`, `OnMaster.yml` (Docker publish), `OnMerge.yml` |
| `executable/src/main/resources/application.properties` | Spring Boot config |

## Useful Links

| Link | Purpose |
|------|---------|
| http://localhost:8080 | Application (when running) |
| http://localhost:5173 | Frontend dev server (when running; it proxies `/logic` to the backend on 8080) |
| http://localhost:8080/actuator/health | Health check |
| http://localhost:8080/logic/classical/actions | Classical logic actions |
| http://localhost:8080/logic/modal/actions | Modal logic actions |
| http://localhost:8080/logic/classical/exercises | Classical logic exercises |
| [GitHub](https://github.com/dan323/natural-deduction) | Repository |
| [SonarCloud](https://sonarcloud.io/project/overview?id=natural-deduction) | Code quality |

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
- **Spring Boot**: 3.5.16
- **Maven**: 3.6.3+
- **Node.js**: 20.19+
- **React**: 19
- **TypeScript**: 5.9

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Tests fail after pulling | `mvn clean install` |
| Port 8080 in use | Kill process or change port |
| Frontend can't connect | Ensure backend running on 8080 (the dev server proxies `/logic` there) |
| Jar serves no UI | Copy `frontend/build/*` into `executable/src/main/resources/public/` and rebuild |
| npm install errors | Delete node_modules and run `npm ci` |
| Maven build fails | Check Java version is 21+ |

## Performance Tips

1. Use `-DskipTests` during development if tests are slow
2. Use IDE debugger for profiling
3. Monitor memory with `jcmd`
4. Run a single module with `mvn test -pl <module>`
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
