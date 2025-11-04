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

Database Schema

The notification service uses PostgreSQL database `as_notification_service`. Tables are created automatically via `deployment/postgres/init-db.sql` on first startup. See `POSTGRES_SETUP.md` for database configuration.

More

See `DEV_GUIDE.md` and `POSTGRES_SETUP.md` for developer quickstarts and database guidance.
