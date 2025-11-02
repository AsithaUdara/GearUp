# user-auth-service

Short guide: build, test and run the `user-auth-service` module in this monorepo.

Requirements

- Java 21 (or use the Maven wrapper: `mvnw.cmd` on Windows)
- Docker Desktop (for running via compose)

Build & test

From the repo root:

```powershell
# run unit tests for this module
.\mvnw.cmd -pl services/user-auth-service -am test

# or package without tests
.\mvnw.cmd -pl services/user-auth-service -am -DskipTests package
```

Run locally (Docker Compose)

```powershell
# Build the user-auth-service image and start the service (detached)
docker compose -f deployment/docker/docker-compose.yml build user-auth-service --progress=plain
docker compose -f deployment/docker/docker-compose.yml up --build -d user-auth-service

# Follow logs
docker compose -f deployment/docker/docker-compose.yml logs -f user-auth-service

# If you use the legacy docker-compose binary, replace `docker compose` with `docker-compose`.
```

Endpoints

- Example health endpoint (adjust paths per service):

```
GET http://localhost:8080/api/auth/health
```

Database migrations

Flyway migrations for this service (if any) live in `services/user-auth-service/src/main/resources/db/migration`.

Run migrations with the helper:

```powershell
.\scripts\run-flyway-locally.ps1 -Service user-auth-service -DbPassword 'changeme'
```

More

See `DEV_GUIDE.md` and `docs/POSTGRES_AND_MIGRATIONS.md` for developer quickstarts and migration guidance.
