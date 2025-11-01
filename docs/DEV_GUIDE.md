GearUp Backend — Developer Guide

## Purpose

This guide is for new developers on the GearUp-backend monorepo. It explains the repository layout, prerequisites, step-by-step setup, how to build and run the full stack locally, how to create a new microservice module, and how secrets are handled.

Contents

- Quick start (build & run)
- Repository layout and module purposes
- Prerequisites and tools
- Configure secrets and .env
- Build & run (production and dev flows)
- How to add a new microservice (step-by-step)
- Common troubleshooting
- Useful commands and CI notes

## Quick start (fast): build and run locally

1. Open PowerShell and change to the repository root (example path):

```powershell
cd "D:\University Academic Materials\EAD\GearUp-backend"
```

2. Build the whole Maven reactor (this uses the repository's Maven wrapper):

```powershell
.\mvnw.cmd -T1C -DskipTests -DskipITs package
```

3. Start the full stack with Docker Compose (production compose):

```powershell
docker compose -f deployment/docker/docker-compose.yml build --progress=plain
docker compose -f deployment/docker/docker-compose.yml up --build -d
```

4. Check running containers:

```powershell
docker compose -f deployment/docker/docker-compose.yml ps
```

## Repository layout and module purposes

Top-level modules and important folders:

- `api-gateway/` — Edge gateway, routes requests to microservices; contains Spring Cloud Gateway filters and authentication wiring.
- `config-server/` — Spring Cloud Config server; serves the microservice YAML configuration from `config-repo/`.
- `config-repo/` — YAML config files for each service (used by Config Server in local setup).
- `deployment/` — Docker Compose and Kubernetes manifests for local and cloud deployment. `deployment/docker/docker-compose.yml` is the primary compose file.
- `services/` — Each microservice lives in `services/<service-name>/` (e.g., `automobile-service`, `notification-service`, `user-auth-service`). Each has its own `pom.xml` and `Dockerfile`.
- `shared-libs/` — Shared Java libraries used across services (e.g., `shared-security-lib`, `shared-event-models`).
- `scripts/` — helpful scripts (example: `run-flyway-locally.ps1`).

## Prerequisites & Tools (what you need locally)

- Git (to clone repository)
- Java JDK 21 installed and JAVA_HOME set
- Docker Desktop (with WSL 2 back-end or suitable Docker Engine)
  - Ensure Docker Compose (v2) is available: `docker compose` command
- PowerShell (Windows PowerShell or PowerShell Core)
- (Optional) IDE: IntelliJ IDEA or VS Code with Java extensions
- Network access to Maven Central (for building dependencies) or a corporate mirror

### Install & configure required software

If you don't already have the prerequisites installed, the short instructions and official download links below will get you set up. These notes focus on Windows (PowerShell) since the project owner works on Windows, but the links are cross-platform.

- Git
  - Download: https://git-scm.com/downloads
  - Install normally. After install, verify with:

```powershell
git --version
```

- Java JDK 21 (Eclipse Temurin / Adoptium)
  - Download: https://adoptium.net (select Temurin 21)
  - Install and set `JAVA_HOME` (example on Windows PowerShell):

```powershell
# set for current session
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21'
# add to PATH for the session
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
# To persist, update system environment variables via Windows Settings or use setx
```

- Docker Desktop (Windows)

  - Download: https://www.docker.com/products/docker-desktop
  - Install and enable WSL 2 integration (recommended). After installing, start Docker Desktop and ensure it is healthy.
  - Verify: `docker version` and `docker compose version`.
  - Shared folders: If you need bind mounts from Windows into containers (e.g., Firebase JSON), ensure the folder is allowed in Docker Desktop Settings -> Resources -> File Sharing.

- PostgreSQL client (psql)
  - On Windows you can install the PostgreSQL client from https://www.postgresql.org/download/windows/ (or use `psql` bundled in Docker images). Verify with:

```powershell
psql --version
```

- pgAdmin (optional, GUI)

  - Download: https://www.pgadmin.org/download/
  - Alternatively run the official container: `dpage/pgadmin4` (see `docs/POSTGRES_AND_MIGRATIONS.md` for the quick docker run example).

- IDE (recommended)
  - IntelliJ IDEA: https://www.jetbrains.com/idea/
  - VS Code: https://code.visualstudio.com/ (install Java Extension Pack)

Notes

- The repository includes the Maven wrapper (`mvnw` / `mvnw.cmd`) so you don't need a global Maven installation. Use `.
mvnw.cmd` on Windows.
- If you rely on corporate proxies/firewalls, ensure the build host can reach Maven Central or configure a mirror.

## Configure secrets and environment variables

This repo uses a `.env` file at the repo root to keep local development environment variables and secret references. DO NOT commit secrets to Git. Instead:

1. Copy `.env.example` (if present) or create `.env` at the repo root:

```powershell
copy .env.example .env
# or create .env manually and fill values
```

2. Common variables you must set in `.env`:

- `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` — DB credentials.
- `FIREBASE_CREDENTIALS_HOST_PATH` — absolute path on your Windows host where the Firebase service account JSON is stored, e.g. `C:\SecureKeys\gear-up\firebase-service-account.json`.
- `FIREBASE_CREDENTIALS_CONTAINER_PATH` — path inside the container where the file will be mounted, usually `/run/secrets/firebase-service-account.json`.

3. Save Firebase JSON only on the developer machine in a secure folder. On Docker Desktop for Windows, ensure that folder is shared (Docker Desktop > Settings > Resources > File Sharing) so it can be bind-mounted into containers.

Using Docker secrets (Kubernetes / Production)

- For production or CI pipelines, use a secrets manager (Vault, Azure Key Vault, AWS Secrets Manager, or Kubernetes secrets). The compose file supports bind mounts for local dev but production should use secret stores.

## Build & run - production vs dev flows

Production (build inside Docker images using multi-stage Dockerfiles)

- Recommended when images will be pushed to a registry:

```powershell
.\mvnw.cmd -DskipTests package  # local packaging is optional but image builds run mvnw inside builder stage
docker compose -f deployment/docker/docker-compose.yml build --progress=plain
docker compose -f deployment/docker/docker-compose.yml up --build -d
```

Dev flow (fast local iteration - copy local jars into runtime image)

- This repo has a dev helper `dev-up.ps1` and a `docker-compose.dev.yml` override. The dev flow first builds jars locally then uses `Dockerfile.dev` to make tiny runtime images that copy built jars.

```powershell
.\dev-up.ps1
# or manually:
.\mvnw.cmd -DskipTests package
docker compose -f deployment/docker/docker-compose.yml -f deployment/docker/docker-compose.dev.yml up --build -d
```

## How to add a new microservice (step-by-step)

This section shows the minimal steps to add a new microservice to the monorepo.

1. Create module folder under `services/`:
   - `services/my-service/`
2. Add Spring Boot app (use start.spring.io or a minimal starter) and create `pom.xml` with the repo parent as parent POM. Keep groupId/com.gearup naming consistent.
3. Add `Application` class and Spring Boot dependencies. Use `shared-libs` where appropriate.
4. Add a `Dockerfile` using the pattern found in existing services. Minimal multi-stage pattern:

```dockerfile
# builder
FROM openjdk:21-jdk as builder
WORKDIR /workspace
COPY . /workspace
RUN ./mvnw -B -DskipTests -pl services/my-service -am package

# runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /workspace/services/my-service/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

5. Add the module to the parent POM so `mvn` reactor builds it. Edit root `pom.xml` if modules are listed.
6. Add a config entry in `config-repo/` (if your service needs config) — e.g., `my-service.yml`.
7. Add an entry to `deployment/docker/docker-compose.yml` so you can run it locally. Follow the pattern used by other services for environment, ports, and volumes.
8. Add any necessary routes in `api-gateway` (if the service should be proxied by the gateway).
9. Add tests and documentation.

## Secrets and credentials

- Never commit JSON service accounts or other secrets to the repo.
- For local dev, use the `.env` and bind-mount secrets into containers (see `FIREBASE_CREDENTIALS_HOST_PATH`).
- For production, store secrets in a secure secrets manager and provide CI with read access to push them into the cluster or into Docker secrets.

## Common Troubleshooting

- "env file not picked up" — remember `env_file` in docker-compose is resolved relative to the compose file itself. Use an absolute path or a repo-root relative path (we use `../../.env` inside `deployment/docker/docker-compose.yml`).
- "invalid spec: :/run/secrets/...:ro: empty section between colons" — caused by empty env substitution (missing FIREBASE_CREDENTIALS_HOST_PATH). Fill `.env` or provide `--env-file`.
- Docker can't pull specific Maven image tag — fallback approach used: builder stage uses `openjdk` and runs `./mvnw` from the repo (this requires `./mvnw` to be executable inside the image).
- Permission error running `./mvnw` inside the builder (Linux): ensure the wrapper is executable (`git update-index --chmod=+x mvnw` before building) or call `sh ./mvnw` in Dockerfile.

## Useful commands

- Build whole project locally (fast):

```powershell
.\mvnw.cmd -T1C -DskipTests -DskipITs package
```

## How to test

Quick testing commands and tips (PowerShell):

- Run all unit tests for the repo:

```powershell
.\mvnw.cmd test
```

- Run tests for a single service (from repo root):

```powershell
.\mvnw.cmd -pl services/notification-service -am test
```

- Run integration/migration checks (Postgres + Flyway):

```powershell
# start postgres for migrations
docker compose -f deployment/postgres/docker-compose.yml up -d

# create DBs/users and apply migrations for all services
.\scripts\run-flyway-locally.ps1 -UseCompose -DbPassword 'changeme'
```

- Smoke-test running services via Docker Compose:

```powershell
docker compose -f deployment/docker/docker-compose.yml up --build -d
docker compose -f deployment/docker/docker-compose.yml logs -f notification-service
# then curl or call sample endpoints from each service README
```

Links:

- Detailed DB/Flyway instructions: `docs/POSTGRES_AND_MIGRATIONS.md`
- Per-service testing: see `services/<service>/README.md`
- Build single service image (production Dockerfile):

```powershell
docker compose -f deployment/docker/docker-compose.yml build notification-service --progress=plain
```

- Follow a service log:

```powershell
docker compose -f deployment/docker/docker-compose.yml logs -f notification-service
```

- Stop and remove everything:

```powershell
docker compose -f deployment/docker/docker-compose.yml down --remove-orphans
```

## Code quality & static analysis

- PMD rules are in the build. Run `mvn pmd:check` in a module to check issues locally.
- SpotBugs may need network access during plugin resolution; in CI ensure plugin caching or allow network.

I can add short, focused READMEs per service or generate a developer quickstart printed in the root README. Tell me if you want the more concise README replaced or the existing `README.md` updated.

## Postgres & migrations

Detailed Postgres and Flyway migration instructions live in `docs/POSTGRES_AND_MIGRATIONS.md`. It includes the compose file location, the helper script `scripts/run-flyway-locally.ps1`, CI examples, and step-by-step commands.

---

If you want, I can now:

- Create per-service README templates in `services/<service>/README.md`.
- Remove the dev artifacts once you confirm you want production Dockerfiles only.

If that sounds good, I'll add two more README files: one `services/README.md` with a step-by-step microservice template, and `deployment/docker/README.md` explaining compose files and secrets.
