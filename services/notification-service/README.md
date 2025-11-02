# notification-service

Short guide: build, test and run the `notification-service` module in this monorepo.

Requirements

- Java 21 (or use the Maven wrapper: `mvnw.cmd` on Windows)
- Docker Desktop (for running via compose)

Build & test

From the repo root:

```powershell
# build and run unit tests for this module
.\mvnw.cmd -pl services/notification-service -am test

# or package without tests
.\mvnw.cmd -pl services/notification-service -am -DskipTests package
```

Run locally (Docker Compose)

```powershell
# Build the notification-service image and start the service (detached)
docker compose -f deployment/docker/docker-compose.yml build notification-service --progress=plain
docker compose -f deployment/docker/docker-compose.yml up --build -d notification-service

# Follow logs
docker compose -f deployment/docker/docker-compose.yml logs -f notification-service

# If you use the legacy docker-compose binary, replace `docker compose` with `docker-compose`.
```

Endpoints

- Once running, consult the service logs or API gateway routes for the reachable endpoints. Example:

```
GET http://localhost:8080/api/notification/health
```

Database migrations

Flyway migrations for this service live in `services/notification-service/src/main/resources/db/migration` (if present). Use the repo helper to run migrations:

```powershell
.\scripts\run-flyway-locally.ps1 -Service notification-service -DbPassword 'changeme'
```

More

See `DEV_GUIDE.md` and `docs/POSTGRES_AND_MIGRATIONS.md` for developer quickstarts and migration guidance.
