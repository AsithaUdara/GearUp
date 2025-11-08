Docker deployment — how to run the GearUp stack locally

This file explains the Compose files and secrets mounting used by the repository.

## Install Docker Desktop

If you don't already have Docker installed, Docker Desktop is the recommended option on Windows and macOS.

- Download: https://www.docker.com/products/docker-desktop
- After installation on Windows, enable WSL 2 integration (Docker Desktop > Settings > Resources > WSL Integration) for best performance.
- Ensure the folder(s) you plan to mount into containers (for example the host path to the Firebase JSON) are allowed in Docker Desktop Settings -> Resources -> File Sharing.

Verify installation:

```powershell
docker version
docker compose version
```

Compose files

- `deployment/docker/docker-compose.yml` — primary production-style compose file. Services are defined with build contexts and production Dockerfiles.
- `deployment/docker/docker-compose.dev.yml` — optional dev override that uses `Dockerfile.dev` (copies local built jars into a runtime image). This is provided for quick local iteration.

Environment variables and `.env`

- The `deployment/docker/` directory contains `.env` with environment variables used by Docker Compose services.
- **IMPORTANT CHANGE**: Environment variables have been reorganized:
  - **`deployment/docker/.env`** — Docker-specific variables (database credentials, RabbitMQ, Redis, container paths)
  - **Root `.env`** — Local development variables (Firebase host paths, IDE configs)
- Always create `.env` locally and never commit secrets. Use `.env.example` as a template:
  ```powershell
  # In deployment/docker directory
  Copy-Item .env.example .env
  ```
- Example Docker variables (in `deployment/docker/.env`):
  - `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_RABBITMQ_HOST`, `SPRING_RABBITMQ_PORT`, `SPRING_RABBITMQ_USERNAME`, `SPRING_RABBITMQ_PASSWORD`
  - `SPRING_REDIS_HOST`, `SPRING_REDIS_PORT`
  - Service-specific DB URLs and credentials (`AUTOMOBILE_DB_URL`, `NOTIFICATION_DB_URL`, etc.)
- Example local development variables (in root `.env`):
  - `FIREBASE_CREDENTIALS_HOST_PATH` — path on your host to Firebase JSON file (e.g., `C:\SecureKeys\gear-up\firebase-service-account.json`)
  - `APP_FIREBASE_CONFIGURATION_FILE` — for local Spring Boot applications

Firebase credentials and mounts

- In Docker Compose, we mount the host Firebase JSON into containers as a file under `/run/secrets/...`.
- If the host path is blank, you may see errors like "invalid spec: :/run/secrets/...:ro: empty section between colons" — this means the env var used for the host path is empty.
- **Note**: Firebase credentials are handled differently for Docker vs local development:
  - **Docker**: Uses `FIREBASE_CREDENTIALS_CONTAINER_PATH` (defined in `deployment/docker/.env`)
  - **Local development**: Uses `FIREBASE_CREDENTIALS_HOST_PATH` (defined in root `.env`)
- To fix Docker errors:
  1. Ensure `deployment/docker/.env` has valid Firebase configuration.
  2. Ensure your Firebase JSON file is accessible to Docker Desktop (File Sharing settings on Windows).

Production vs Dev compose flows

- Production flow expects CI (or a local root-level Maven build) to produce service jars under `target/` and Dockerfiles copy those prebuilt jars into the runtime image. This avoids running Maven inside the image and makes image builds faster and more reproducible.
- Dev helper flow (legacy): `docker-compose.dev.yml` and `Dockerfile.dev` were previously provided to copy locally built jars into a runtime image for quick iteration. Those files are now deprecated in this repo; the primary compose file is `deployment/docker/docker-compose.yml` and the recommended local flow is to run a root `./mvnw -DskipTests package` before starting compose so images can pick up the built artifacts.

Common commands (PowerShell)

```powershell
# Production-style build and run
.\mvnw.cmd -DskipTests package
docker compose -f deployment/docker/docker-compose.yml build --progress=plain
docker compose -f deployment/docker/docker-compose.yml up --build -d

# Dev quickflow (build jars locally then run with dev override)
.\mvnw.cmd -DskipTests package
docker compose -f deployment/docker/docker-compose.yml -f deployment/docker/docker-compose.dev.yml up --build -d

# Rebuild single service
docker compose -f deployment/docker/docker-compose.yml build notification-service --progress=plain

# View logs
docker compose -f deployment/docker/docker-compose.yml logs -f notification-service
```

Permissions & mvnw

- If you see permission errors when Docker tries to run `./mvnw` inside a builder container, add `RUN chmod +x ./mvnw` in the Dockerfile builder stage or adjust the COPY step to preserve permissions. On Windows this is rarely needed, but Linux container environments sometimes require it.

Removing dev artifacts

- If you prefer not to use the dev helper flow, you can safely remove `deployment/docker/docker-compose.dev.yml`, `dev-up.ps1`, and `services/*/Dockerfile.dev` once the production Dockerfiles are verified to work for your team. I can remove those on request.

If you'd like, I can also add a small example `docker-compose` override that only starts a single service plus its dependencies to speed up local development. Say "add single-service override" and I'll add it.

## Postgres Initialization

Database initialization is handled centrally via `deployment/postgres/init-db.sql`. The init script creates per-service databases, users, and initial tables on first container startup.

See `POSTGRES_SETUP.md` and `deployment/postgres/init-db.sql` for details and examples. For local verification, use the helper script:

```powershell
.\scripts\test-db-connections.ps1
```
