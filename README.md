# loomify

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/dallay/loomify)

A Spring Boot application built with Kotlin and Gradle, providing a robust backend infrastructure.

## Prerequisites

- JDK 21 or later
- Gradle 8.x or later (or use the included Gradle wrapper)
- Docker and Docker Compose (for running the PostgreSQL database)
- Git

## Project Setup

### Clone the Repository

```bash
git clone <repository-url>
cd loomify
```

### Build the Project

```bash
./gradlew build
```

## Running the Application

### Using Gradle

```bash
./gradlew bootRun
```

### Using Java

```bash
./gradlew build
java -jar build/libs/loomify-0.0.1-SNAPSHOT.jar
```

## Database Configuration

The application uses PostgreSQL as its database. The connection is configured in `application.yml`.

### Using Docker Compose

The project includes a `compose.yaml` file that sets up the required infrastructure services:

- **PostgreSQL**: Database server on port 5432
- **Keycloak**: Authentication server on port 9080
- **GreenMail**: Email testing server on port 3025 (SMTP) and 8080 (Web UI)

```bash
# Start all infrastructure services
docker compose up -d

# Stop all services
docker compose down

# Start only specific services
docker compose up -d postgresql greenmail
```

Alternatively, Spring Boot's Docker Compose support will automatically start the required containers when you run the application.

## Database Migrations

This project uses Liquibase for database migrations. The changelog file is located at:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

## Git Hooks with Lefthook

This project uses [Lefthook](https://github.com/evilmartians/lefthook) to manage Git hooks. Lefthook is a fast and powerful Git hooks manager that allows for parallel execution of commands and is configurable via a simple YAML file.

### Installation

To install Lefthook and enable the Git hooks, run the following command from the root of the repository:

```bash
pnpm install
```

The `prepare` script in `package.json` will automatically run `lefthook install`.

### Pre-commit Hooks

The following tasks are run automatically on `git commit`:

- **Biome**: Runs the lint script defined in package.json using Biome. If linting issues are found, the commit will be blocked until they are fixed.
- **Changed Files Summary**: Shows a summary of changed files.
- **Git Update**: Updates the Git index to ensure all changes are included.

### Pre-push Hooks

The following tasks are run automatically on `git push` (now running in parallel for improved performance):

- **Link Check**: Checks for broken links in Markdown files using Lychee.
- **PNPM Check**: Runs the check script defined in package.json.
- **Kotlin Static Analysis**: Runs Detekt for static code analysis.
- **Secret Check**: Checks for secrets in the codebase.
- **Tests**: Runs both frontend and backend tests.
- **Builds**: Builds both frontend and backend projects (skipping tests as they're run separately).

## Development Workflow

1. Make changes to the code
2. Run tests: `./gradlew test` and `pnpm test`
3. Build the application: `./gradlew build` and `pnpm build`
4. Run the application: `./gradlew bootRun` and `pnpm dev`

## Testing

### Running Tests

```bash
./gradlew test
```

### Integration Tests with Testcontainers

The project uses Testcontainers for integration tests, which automatically spin up Docker containers for testing.

```bash
./gradlew integrationTest
```

## Project Structure

- `src/main/kotlin` - Kotlin source files
- `src/main/resources` - Configuration files and resources
- `src/test/kotlin` - Test source files
- `.github/actions` - Custom GitHub Actions for CI/CD
  - `.github/actions/docker` - Specialized Docker composition actions
  - `.github/actions/setup` - Setup actions for Java and Node.js
- `.github/workflows` - GitHub Actions workflows for CI/CD

## Key Features

- Spring Boot 3.4.5
- Kotlin 1.9.25
- Reactive programming with Spring WebFlux
- OAuth2 Authorization Server with Keycloak integration
- Spring Security
- Spring Data R2DBC for reactive database access
- Email testing with GreenMail
- Internationalization (i18n) support with message bundles
- Liquibase for database migrations
- Spring Boot Admin for application monitoring
- Docker Compose support
- Testcontainers for integration testing

## API Documentation

API documentation is generated using Spring REST Docs and is available at:

```shell
build/generated-snippets
```

## Monitoring

The application includes Spring Boot Actuator endpoints for monitoring:

- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus metrics: `/actuator/prometheus`

## CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment:

### Docker Composition Actions

The CI/CD pipeline uses specialized Docker composition actions for building and pushing Docker images:

- **Backend Docker Action**: Builds Spring Boot applications using Gradle's `bootBuildImage`
- **Frontend Web App Action**: Builds Vue.js applications with multi-stage builds
- **Frontend Landing Page Action**: Builds Astro static sites with optimized configurations
- **Security Scanning Action**: Scans Docker images for vulnerabilities using Trivy

For detailed documentation on these actions, see:

- [Docker Composition Actions Documentation](docs/workflows/docker-composition-actions.md)
- [Docker Actions Migration Guide](docs/workflows/docker-actions-migration-guide.md)

### Workflows

- `deploy.yml`: Builds and deploys all application components
- `backend-ci.yml`: Runs tests and static analysis for backend code
- `frontend-ci.yml`: Runs tests and static analysis for frontend code
