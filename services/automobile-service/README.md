````markdown
# automobile-service

Short guide: build, test and run the `automobile-service` module in this monorepo.

Requirements

- Java 21 (or use the Maven wrapper: `mvnw.cmd` on Windows)
- Docker Desktop (for running via compose)

Build & test

From the repo root:

```powershell
# build and run unit tests for this module
.\mvnw.cmd -pl services/automobile-service -am test

# or package without tests
.\mvnw.cmd -pl services/automobile-service -am -DskipTests package
```
````

Run locally (Docker Compose)

```powershell
# Build the automobile-service image and start the service (detached)
docker compose -f deployment/docker/docker-compose.yml build automobile-service --progress=plain
docker compose -f deployment/docker/docker-compose.yml up --build -d automobile-service

# Follow logs
docker compose -f deployment/docker/docker-compose.yml logs -f automobile-service

# If you use the legacy docker-compose binary, replace `docker compose` with `docker-compose`.
```

Endpoints

- Check the module README or `DEV_GUIDE.md` for common endpoints once service is running. Example (gateway may proxy):

```
GET http://localhost:8080/api/automobile-service/actuator/health
```

Database initialization

This repository no longer uses Flyway migrations. Databases and initial schema are created by the centralized DB init script mounted into the Postgres container.

Steps to add or update schema for this service:

1. Edit `deployment/postgres/init-db.sql` and add the appropriate CREATE DATABASE / CREATE USER / CREATE TABLE statements for `as_automobile_service`.

```sql
-- Example (already present):
-- CREATE DATABASE as_automobile_service;
-- CREATE USER auto_user WITH PASSWORD 'auto_secure_pass_123';
-- GRANT ALL PRIVILEGES ON DATABASE as_automobile_service TO auto_user;
```

2. Update `.env` with the automobile DB connection variables (if not already present):

```text
AUTOMOBILE_DB_URL=jdbc:postgresql://db:5432/as_automobile_service
AUTOMOBILE_DB_USER=auto_user
AUTOMOBILE_DB_PASSWORD=auto_secure_pass_123
```

3. Restart the DB container so the init script runs on a fresh volume (or manually run the SQL against the DB):

```powershell
docker compose -f deployment/docker/docker-compose.yml down -v
docker compose -f deployment/docker/docker-compose.yml up -d db
```

4. Start the service (or full stack):

```powershell
docker compose -f deployment/docker/docker-compose.yml up -d automobile-service
```

Testing

```powershell
# Health check
curl http://localhost:8082/actuator/health

# Via API Gateway (if gateway running)
curl http://localhost:8080/api/automobile-service/vehicles

# Verify DB connection (script)
.\scripts\test-db-connections.ps1
```

More

See `POSTGRES_SETUP.md`, `README.md` (root) and `DEV_GUIDE.md` for developer quickstarts and database guidance.

```

```
