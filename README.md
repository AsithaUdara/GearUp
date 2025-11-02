# GearUp Automobile Service Backend

Secure Spring Boot backend with Firebase Authentication and environment-based secret management.

## Features

- Firebase Admin SDK initialization from `.env` (no secrets in repo)
- Stateless Spring Security with custom `FirebaseFilter`
- CORS configuration for local frontend
- Layered package structure (`config`, `security`, `controller`)

## Requirements

- Java 21
- Use Maven Wrapper (`mvnw.cmd`) if Maven not installed

## Monorepo layout (new)

This repository has been reorganized into a monorepo-style microservice layout. Top-level folders now include:

```
api-gateway/
service-discovery/
config-server/
services/
	├─ automobile-service/   # previously `main`
	└─ notification-service/ # previously `notification-service`
shared-libs/
deployment/
docs/
```

Local docker-compose for quick dev is at `deployment/docker/docker-compose.yml` and has been updated to reference the services under `services/`.

## Setup

1. Copy `.env.example` to `.env` at the repository root and set the required values.

2. Provide your Firebase service account JSON file on the host and set its absolute path in `.env`:

```
# Example (Windows)
FIREBASE_CREDENTIALS_HOST_PATH=C:\\SecureKeys\\gear-up\\firebase-service-account.json

# Example (Linux/macOS)
FIREBASE_CREDENTIALS_HOST_PATH=/run/secrets/firebase-service-account.json
```

3. Optionally adjust the container path where the file will be mounted (default is `/run/secrets/firebase-service-account.json`):

```
FIREBASE_CREDENTIALS_CONTAINER_PATH=/run/secrets/firebase-service-account.json
```

4. The docker-compose services will expose the mounted file inside the container and set the environment variable `APP_FIREBASE_CONFIGURATION_FILE=file:${FIREBASE_CREDENTIALS_CONTAINER_PATH}` so Spring Boot picks it up.

## Run

You can run the services locally in Docker (recommended for microservices) or run modules individually with Maven.

Docker (recommended):

1. Ensure Docker Desktop / daemon is running.
2. Copy `.env.example` to `.env` and set `FIREBASE_CREDENTIALS_HOST_PATH` to the host path of your Firebase JSON.
3. From the repo root run with Docker Compose (cross-platform):

Note: the repository's compose files live under `deployment/docker/`. You must point Docker Compose at that file (or run the commands from that folder). Below are the recommended cross-platform commands and alternatives for older Docker installations.

```powershell
# Preferred (Docker Compose v2 - `docker compose`)
# Build images and start containers in detached mode
docker compose -f deployment/docker/docker-compose.yml up --build -d

# View logs (follow)
docker compose -f deployment/docker/docker-compose.yml logs -f

# Stop and remove containers
docker compose -f deployment/docker/docker-compose.yml down
```

If you have the legacy `docker-compose` binary instead of the newer `docker compose` CLI, use the same commands but with a hyphen:

```powershell
docker-compose -f deployment/docker/docker-compose.yml up --build -d
docker-compose -f deployment/docker/docker-compose.yml logs -f
docker-compose -f deployment/docker/docker-compose.yml down
```

If you prefer to run services directly via Maven (dev mode):

```bash
# From the module folder (example: main)
./mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run       # macOS/Linux (if using the unix wrapper)

# Or build and run the jar
./mvnw.cmd clean package     # Windows
./mvnw clean package         # macOS/Linux
java -jar main/target/demo-0.0.1-SNAPSHOT.jar
```

## DB & migrations (quick-start)

This project uses PostgreSQL and Flyway for schema migrations. Quick steps to get started locally:

1. Start a local Postgres container (the repo includes a compose at `deployment/postgres/docker-compose.yml`):

```powershell
docker compose -f deployment/postgres/docker-compose.yml up -d
```

2. Run the repo helper to create per-service DBs/users and apply Flyway migrations:

```powershell
.\scripts\run-flyway-locally.ps1 -UseCompose -DbPassword 'changeme'
```

3. Migration files are detected in each service at `services/<service>/src/main/resources/db/migration` (Flyway naming: `V1__init.sql`, `V2__...`).

4. For a single-service migration you can run:

```powershell
.\scripts\run-flyway-locally.ps1 -Service notification-service -DbPassword 'changeme'
```

More detailed instructions and CI examples are in `docs/POSTGRES_AND_MIGRATIONS.md`.

## How to test

Short test guide and quick commands (PowerShell):

- Run unit tests for the whole repo:

```powershell
.\mvnw.cmd test
```

- Run tests for a single service (from repo root):

```powershell
.\mvnw.cmd -pl services/automobile-service -am test
```

- Apply Flyway migrations locally (quick verification):

```powershell
docker compose -f deployment/postgres/docker-compose.yml up -d
.\scripts\run-flyway-locally.ps1 -UseCompose -DbPassword 'changeme'
```

- Quick smoke test using Docker Compose (build & run services):

```powershell
docker compose -f deployment/docker/docker-compose.yml up --build -d
docker compose -f deployment/docker/docker-compose.yml logs -f notification-service
# then call endpoints listed under each service README
```

See `DEV_GUIDE.md` for a slightly more detailed developer quickstart and `docs/POSTGRES_AND_MIGRATIONS.md` for Flyway examples.

## Test Endpoints

Public: `GET http://localhost:8080/api/public/hello`
Secure: `GET http://localhost:8080/api/secure/hello` with header:

```
Authorization: Bearer <firebase-id-token>
```

## Troubleshooting

| Symptom                  | Cause                   | Fix                       |
| ------------------------ | ----------------------- | ------------------------- |
| 401 Unauthorized         | Invalid / expired token | Refresh Firebase ID token |
| Firebase not initialized | Empty `.env`            | Populate JSON correctly   |
| mvn not recognized       | Maven not installed     | Use `mvnw.cmd`            |

## Next Steps (Optional)

- Add SLF4J logging instead of `printStackTrace()`
- Add `spring-boot-starter-actuator`
- Externalize CORS origins via env var
- Write tests for `FirebaseFilter`

---

Professional, secure, and ready for review.
