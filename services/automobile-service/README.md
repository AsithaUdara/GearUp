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

## Database & Migrations

This service uses **Flyway** for version-controlled database migrations. The database and user are created by `deployment/postgres/init-db.sql`, while tables and schema are managed by Flyway migration scripts.

### Database Setup

Database: `as_automobile_service`  
User: `svc_automobile_service`  
Migrations: `src/main/resources/db/migration/`

### Migration Files

- `V1__initial_schema.sql` - Creates vehicles table and indexes

### Running Migrations

```powershell
# Run migrations via Maven
.\mvnw.cmd -pl services/automobile-service flyway:migrate

# Check migration status
.\mvnw.cmd -pl services/automobile-service flyway:info

# Validate migrations
.\mvnw.cmd -pl services/automobile-service flyway:validate
```

### Creating New Migrations

1. Create new SQL file in `src/main/resources/db/migration/`
2. Follow naming: `V{version}__{description}.sql`  
   Example: `V2__add_vehicle_tracking.sql`
3. Write idempotent SQL
4. Test locally before committing

Example migration:

```sql
-- V2__add_vehicle_tracking.sql
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS last_location VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_vehicles_last_location ON vehicles(last_location);
```

### Initial Setup

1. Database and user are created by `deployment/postgres/init-db.sql`
2. On service startup, Flyway automatically:

   - Creates `flyway_schema_history` table
   - Runs pending migrations
   - Validates checksums

3. Update `.env` with DB connection (if needed):

```text
AUTOMOBILE_DB_URL=jdbc:postgresql://db:5432/as_automobile_service
AUTOMOBILE_DB_USER=svc_automobile_service
AUTOMOBILE_DB_PASSWORD=auto_svc_pass_2024
```

4. Start the service:

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
