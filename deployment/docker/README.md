Docker deployment — how to run the GearUp stack locally

This file explains the Compose files and secrets mounting used by the repository.

Compose files

- `deployment/docker/docker-compose.yml` — primary production-style compose file. Services are defined with build contexts and production Dockerfiles.
- `deployment/docker/docker-compose.dev.yml` — optional dev override that uses `Dockerfile.dev` (copies local built jars into a runtime image). This is provided for quick local iteration.

Environment variables and `.env`

- The repo root contains `.env` with environment variables used by compose. The compose file references the file with a path relative to `deployment/docker/` so Docker Compose reads the repo-root `.env` correctly.
- Always create `.env` locally and never commit secrets. Example variables:
  - `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
  - `FIREBASE_CREDENTIALS_HOST_PATH` — path on the Windows host to your Firebase JSON file (e.g. `C:\SecureKeys\gear-up\firebase-service-account.json`). This must be accessible to Docker Desktop.

Firebase credentials and mounts

- In local compose we mount the host Firebase JSON into containers as a file under `/run/secrets/...`. If the host path is blank you may see errors like "invalid spec: :/run/secrets/...:ro: empty section between colons" — this means the env var used for the host path is empty.
- To fix:
  1. Ensure `.env` has a valid `FIREBASE_CREDENTIALS_HOST_PATH` set.
  2. Ensure Docker Desktop is allowed to mount the path (File Sharing on Windows / path is accessible).

Production vs Dev compose flows

- Production flow builds images from the repo using multi-stage Dockerfiles. The Dockerfiles run `./mvnw` inside the builder stage so builder images don't rely on pulling a specific Maven image tag.
- Dev flow uses `docker-compose.dev.yml` and `Dockerfile.dev` to copy pre-built jars created by a local `mvn` run — this is faster for iterative development.

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
