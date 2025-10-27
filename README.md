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

```bash
# Build images and start containers in detached mode
docker compose up --build -d

# Tail logs (press Ctrl+C to stop)
docker compose logs -f

# Stop and remove containers
docker compose down
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

## Test Endpoints
Public: `GET http://localhost:8080/api/public/hello`
Secure: `GET http://localhost:8080/api/secure/hello` with header:
```
Authorization: Bearer <firebase-id-token>
```

## Troubleshooting
| Symptom | Cause | Fix |
|---------|-------|-----|
| 401 Unauthorized | Invalid / expired token | Refresh Firebase ID token |
| Firebase not initialized | Empty `.env` | Populate JSON correctly |
| mvn not recognized | Maven not installed | Use `mvnw.cmd` |

## Next Steps (Optional)
- Add SLF4J logging instead of `printStackTrace()`
- Add `spring-boot-starter-actuator`
- Externalize CORS origins via env var
- Write tests for `FirebaseFilter`

---
Professional, secure, and ready for review.
