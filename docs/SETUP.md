# Setup & Installation

This guide covers building, configuring, and running the Natural Deduction project.

## Prerequisites

### System Requirements

- **Java**: JDK 21 or higher
- **Maven**: 3.6.0 or higher
- **Node.js**: 16.x or higher (for frontend)
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
- Generate JAR files
- Create code coverage reports

**Build Time**: Typically 2-5 minutes depending on your internet connection

### 3. Verify the Build

After successful build, you should see:

```
[INFO] BUILD SUCCESS
```

Generated artifacts will be in:
- `executable/target/executable-0.1-SNAPSHOT.jar` - Main application
- Coverage reports in `jacoco-natural-deduction/target/`

## Frontend Setup

### 1. Install Dependencies

```powershell
cd frontend
npm install
```

This installs all npm dependencies specified in `package.json`.

### 2. Build Frontend

```powershell
npm run build
```

This creates optimized production build in `frontend/build/`.

**Note**: The executable JAR includes the frontend build, so this step is only needed if developing the frontend separately.

## Running the Application

### Option 1: Run the Executable JAR (Recommended)

The JAR file includes both the REST API and the frontend:

```powershell
java -jar executable/target/executable-0.1-SNAPSHOT.jar
```

**Output**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v3.3.2)

2024-01-15 10:30:15.123  INFO 1234 --- [main] com.dan323.executable.Application
Application 'executable' is running! Access URLs:
  Local: http://localhost:8080
  Profile: default
```

**Access the application**:
- Frontend: http://localhost:8080
- REST API: http://localhost:8080/api/
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

This starts a development server on http://localhost:3000 with live reloading.

**Note**: The development server needs the backend running separately on port 8080.

## Configuration

### Backend Configuration

Main configuration file: `executable/src/main/resources/application.properties`

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# Application Name
spring.application.name=executable

# Logging Level
logging.level.root=INFO
logging.level.com.dan323=DEBUG

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
```

### Frontend Configuration

Main configuration files:
- `frontend/src/constant.ts` - API endpoints and constants
- `frontend/tsconfig.json` - TypeScript configuration
- `frontend/jest.config.ts` - Jest testing configuration

**API Endpoint Configuration**:

Edit `frontend/src/constant.ts`:

```typescript
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
export const CLASSICAL_API = `${API_BASE_URL}/api/classical`;
export const MODAL_API = `${API_BASE_URL}/api/modal`;
```

## Running Tests

### Backend Tests

**Run all tests**:
```powershell
mvn clean test
```

**Run specific module tests**:
```powershell
mvn test -pl domain/logic-language/framework
```

**Run with coverage**:
```powershell
mvn clean install
```

Coverage reports are generated in: `target/site/jacoco/index.html`

### Frontend Tests

**Run all tests**:
```powershell
cd frontend
npm test
```

**Run with coverage**:
```powershell
npm test -- --coverage
```

Coverage report: `frontend/coverage/lcov-report/index.html`

**Run specific test file**:
```powershell
npm test -- Expressions.test.tsx
```

## Docker Deployment

### Build Docker Image

```powershell
docker build -t natural-deduction:latest .
```

### Run Docker Container

```powershell
docker run -p 8080:8080 natural-deduction:latest
```

Access the application at: http://localhost:8080

### Docker Compose (if available)

```powershell
docker-compose up
```

## Code Quality Analysis

### SonarCloud Analysis

The project uses SonarCloud for continuous code quality monitoring.

**View reports**: https://sonarcloud.io/project/overview?id=dan323_natural-deduction

### Local JaCoCo Coverage

Generate coverage report:

```powershell
mvn clean install
```

View report: Open `jacoco-natural-deduction/target/site/jacoco/index.html` in browser

### Running PIT Mutation Tests

```powershell
mvn org.pitest:pitest-maven:mutationCoverage -pl domain/logic-language/framework
```

## Troubleshooting

### Build Issues

**Issue**: "Cannot find symbol" errors
- **Solution**: Ensure Java 21 is installed: `java -version`
- **Solution**: Clean build: `mvn clean install`

**Issue**: Maven dependencies not downloading
- **Solution**: Check internet connection
- **Solution**: Clear Maven cache: `mvn clean -DmskipTests install`

### Runtime Issues

**Issue**: Port 8080 already in use
- **Solution**: Kill process using the port or change port in `application.properties`
- **PowerShell**: `Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process`

**Issue**: "Frontend not found" when accessing http://localhost:8080
- **Solution**: Ensure frontend is built: `cd frontend && npm run build`
- **Solution**: Rebuild executable: `mvn clean install -pl executable`

**Issue**: Frontend cannot connect to API
- **Solution**: Check backend is running on port 8080
- **Solution**: Check CORS configuration in Spring Boot
- **Solution**: Verify API endpoints in `frontend/src/constant.ts`

### Frontend Issues

**Issue**: Node modules issues
- **Solution**: Clear and reinstall: `rm -r node_modules package-lock.json && npm install`

**Issue**: TypeScript compilation errors
- **Solution**: Check TypeScript version: `npm list typescript`
- **Solution**: Update TypeScript: `npm install --save typescript@latest`

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
npm install
```

### 2. Backend Development

```powershell
# Start Spring Boot application
mvn spring-boot:run -pl executable

# In another terminal, run tests in watch mode
mvn test -pl domain/logic-language/framework -Dtest=YourTestClass -Dtest.watch
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

Access at: http://localhost:3000 (frontend will proxy API calls to backend)

## Integration with IDEs

### IntelliJ IDEA

1. Open project: File → Open → Select project directory
2. Maven will auto-configure
3. Run → Edit Configurations
4. Add configuration:
   - Type: Application
   - Main class: `com.dan323.executable.Application`
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

