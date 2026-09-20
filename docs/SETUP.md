# Setup & Installation

This guide covers building, configuring, and running the Natural Deduction project.

## Prerequisites

### System Requirements

- **Java**: JDK 21 or higher
- **Maven**: 3.6.3 or higher
- **Node.js**: 20.19 or higher (for frontend)
- **npm**: 7.x or higher (for frontend)
- **Git**: For cloning the repository

### Installation Verification

Check your installations:

```powershell
java -version
javac -version
mvn -version
node --version
npm --version
git --version
```

## Backend Setup

### 1. Clone the Repository

```powershell
git clone https://github.com/dan323/natural-deduction.git
cd natural-deduction
```

### 2. Build the Project

Build the entire project including all modules:

```powershell
mvn clean install
```

This will:
- Download all dependencies
- Compile all modules
- Run unit tests
- Package the jars, including the Spring Boot fat jar
- Run the integration tests (`*IT.java`), one of which boots the fat jar on a free port
- Create code coverage reports

**Build Time**: Typically 2-5 minutes depending on your internet connection

### 3. Verify the Build

After successful build, you should see:

```
[INFO] BUILD SUCCESS
```

Generated artifacts will be in:
- `executable/target/executable-0.1-SNAPSHOT.jar` - Main application
- Coverage reports in `jacoco-natural-deduction/target/site/`

## Frontend Setup

### 1. Install Dependencies

```powershell
cd frontend
npm ci
```

This installs the dependencies pinned in `package-lock.json` (use `npm ci`, not `npm install`).

### 2. Build Frontend

```powershell
npm run build
```

This creates the production build in `frontend/build/` (not `dist/`). `npm run typecheck` type-checks the code, which
`vite build` does not.

**Embedding it in the jar**: the jar serves the UI only if the build is copied into
`executable/src/main/resources/public/` (gitignored) before the backend is packaged. This is what CI does before
publishing the Docker image:

```powershell
cp -r frontend/build/* executable/src/main/resources/public/   # create the folder first if it does not exist
mvn clean install
```

Without this, the jar serves only the REST API.

## Running the Application

### Option 1: Run the Executable JAR (Recommended)

The JAR file serves the REST API and, if the frontend was embedded before packaging (see above), the UI:

```powershell
java -jar executable/target/executable-0.1-SNAPSHOT.jar
```

The application listens on port 8080 (`--server.port=9090` changes it).

**Access the application**:
- Frontend: http://localhost:8080 (when embedded in the jar)
- REST API: http://localhost:8080/logic/{logic}/... (see [API.md](./API.md))
- Health check: http://localhost:8080/actuator/health

### Option 2: Run with Maven

```powershell
mvn spring-boot:run -pl executable
```

### Option 3: Development Frontend Server

For frontend-only development with hot reload:

```powershell
cd frontend
npm start
```

This starts a development server on http://localhost:5173 with live reloading.

**Note**: The UI calls relative URLs (`/logic/...`) and `vite.config.ts` proxies `/logic` to `http://localhost:8080`,
so start the backend jar first (or `mvn spring-boot:run -pl executable`) and use the dev server for the UI.

## Configuration

### Backend Configuration

Main configuration file: `executable/src/main/resources/application.properties`. It only enables the actuator
`beans` and `info` endpoints (only `health` is exposed over HTTP by default), so everything else uses Spring Boot's
defaults. Any property can be overridden on the command line, e.g. `--server.port=9090`.

The one application-specific property:

| Property | Default | Meaning |
|----------|---------|---------|
| `natural-deduction.solve-timeout` | `10s` | How long `POST /logic/{logic}/solve` may run before it is answered with a 422 (`5s`, `PT5S`, ...) |

### Frontend Configuration

Main configuration files:
- `frontend/src/constant.ts` - the logic the UI uses (`LOGIC`)
- `frontend/tsconfig.json` - TypeScript configuration
- `frontend/jest.config.ts` - Jest testing configuration

**API Endpoint Configuration**:

The frontend uses relative API paths (e.g. `/logic/...`). When served by the Spring Boot JAR both frontend and API share the same origin, so no extra configuration is needed.

During local frontend development (Vite dev server on port 5173), `vite.config.ts` proxies `/logic` requests to the backend on port 8080:

```typescript
server: {
  proxy: {
    '/logic': 'http://localhost:8080',
  },
},
```

The only frontend constant that controls behaviour is the logic type in `frontend/src/constant.ts`:

```typescript
export const LOGIC: string = "classical"
```

## Running Tests

### Backend Tests

**Run all tests** (`test` skips the `*IT.java` integration tests, `verify` runs them too, as CI does):
```powershell
mvn clean verify
```

**Run specific module tests**:
```powershell
mvn test -pl domain/logic-language/framework
```

**Run with coverage (coverage is collected by default)**:
```powershell
mvn clean install
```

Coverage reports are generated in: `jacoco-natural-deduction/target/site/jacoco/index.html`

### Frontend Tests

**Type check** (CI runs it before the tests):
```powershell
cd frontend
npm run typecheck
```

**Run all tests** (coverage is collected by default):
```powershell
npm test
```

Coverage report: `frontend/coverage/lcov-report/index.html`

**Run specific test file**:
```powershell
npm test -- --testPathPattern=Expressions
```

## Docker Deployment

The `Dockerfile` only copies `executable/target/*.jar`, so build the jar first (with the frontend embedded, see above).

### Build Docker Image

```powershell
docker build -t natural-deduction:latest .
```

### Run Docker Container

```powershell
docker run -p 8080:8080 natural-deduction:latest
```

Access the application at: http://localhost:8080

The image runs as an unprivileged user and has a `HEALTHCHECK` on `/actuator/health`.

### Published image

On every push to `master`, the "Publish Docker image" workflow (`OnMaster.yml`) type-checks and tests the frontend,
embeds it, runs `mvn verify`, builds the image from that jar, smoke-tests it (actions endpoints, UI, non-root user)
and only then pushes it to Docker Hub as `dan323/natural-deduction`.

## Code Quality Analysis

### SonarCloud Analysis

The project uses SonarCloud for continuous code quality monitoring.

**View reports**: https://sonarcloud.io/project/overview?id=natural-deduction

### Local JaCoCo Coverage

Generate coverage report:

```powershell
mvn clean install
```

View report: Open `jacoco-natural-deduction/target/site/jacoco/index.html` in browser

### Running PIT Mutation Tests

```powershell
mvn test-compile org.pitest:pitest-maven:mutationCoverage -pl domain/logic-language/framework
```

## Troubleshooting

### Build Issues

**Issue**: "Cannot find symbol" errors
- **Solution**: Ensure Java 21 is installed: `java -version`
- **Solution**: Clean build: `mvn clean install`

**Issue**: Maven dependencies not downloading
- **Solution**: Check internet connection
- **Solution**: Retry with `mvn clean install -U` to force a re-download of failed artifacts

### Runtime Issues

**Issue**: Port 8080 already in use
- **Solution**: Kill process using the port or change port in `application.properties`
- **PowerShell**: `Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process`

**Issue**: "Frontend not found" when accessing http://localhost:8080
- **Solution**: The jar only serves the UI if the frontend build was copied into `executable/src/main/resources/public/` before packaging (see Frontend Setup), then rebuild: `mvn clean install`

**Issue**: Frontend cannot connect to API
- **Solution**: Check backend is running on port 8080
- **Solution**: With the Vite dev server, the proxy in `frontend/vite.config.ts` forwards `/logic` to port 8080, so the backend must be running there

### Frontend Issues

**Issue**: Node modules issues
- **Solution**: Clear and reinstall: `rm -r node_modules && npm ci`

**Issue**: TypeScript compilation errors
- **Solution**: Check TypeScript version: `npm list typescript`
- **Solution**: Run `npm run typecheck` to see the errors

## Development Workflow

### 1. Setup Development Environment

```powershell
# Clone repository
git clone https://github.com/dan323/natural-deduction.git
cd natural-deduction

# Build entire project
mvn clean install

# Install frontend dependencies
cd frontend
npm ci
```

### 2. Backend Development

```powershell
# Start Spring Boot application
mvn spring-boot:run -pl executable

# In another terminal, run the tests of one module
mvn test -pl domain/logic-language/framework
```

### 3. Frontend Development

```powershell
cd frontend
npm start
```

This starts the development server with hot reload.

### 4. Full Stack Development

Terminal 1 - Backend:
```powershell
mvn spring-boot:run -pl executable
```

Terminal 2 - Frontend:
```powershell
cd frontend
npm start
```

Access at: http://localhost:5173 (Vite proxies `/logic` requests to the backend on port 8080 (configured in `frontend/vite.config.ts`)).

## Integration with IDEs

### IntelliJ IDEA

1. Open project: File → Open → Select project directory
2. Maven will auto-configure
3. Run → Edit Configurations
4. Add configuration:
   - Type: Application
   - Main class: `com.dan323.main.Application`
   - Working directory: `$PROJECT_DIR$`

### VS Code

1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - REST Client

2. Create `.vscode/launch.json` with Spring Boot launch config

## Next Steps

- [Architecture Overview](./ARCHITECTURE.md) - Understand system design
- [Project Modules](./MODULES.md) - Learn about components
- [Development Guide](./DEVELOPMENT.md) - Start contributing
- [API Reference](./API.md) - Explore REST endpoints

## Additional Resources

- [GitHub Repository](https://github.com/dan323/natural-deduction)
- [Issues Tracker](https://github.com/dan323/natural-deduction/issues)
- [CI/CD Pipeline](https://github.com/dan323/natural-deduction/actions)

