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
GET http://localhost:8081/api/notification/health
```

## Database & Migrations

The notification service uses PostgreSQL database `as_notification_service` with **Flyway** for schema management.

### Database Connection

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/as_notification_service
spring.datasource.username=svc_notification_service
spring.datasource.password=notif_svc_pass_2024
```

### Flyway Migrations

Migrations are located in `src/main/resources/db/migration/` and run automatically when the application starts.

**Run migrations manually:**

```powershell
# From repo root
.\mvnw.cmd -pl services/notification-service flyway:migrate

# Check migration status
.\mvnw.cmd -pl services/notification-service flyway:info

# Validate migrations
.\mvnw.cmd -pl services/notification-service flyway:validate
```

**Creating new migrations:**

1. Create a new file: `src/main/resources/db/migration/V{version}__{description}.sql`

   - Example: `V2__add_notification_templates.sql`

2. Write idempotent SQL:

   ```sql
   ALTER TABLE notifications
   ADD COLUMN IF NOT EXISTS template_id UUID;

   CREATE INDEX IF NOT EXISTS idx_notifications_template
   ON notifications(template_id);
   ```

3. Run the migration:
   ```powershell
   .\mvnw.cmd -pl services/notification-service flyway:migrate
   ```

**Database Tables:**

- `notifications` - Stores notification records
- `flyway_schema_history` - Tracks migration history

See `FLYWAY_INTEGRATION.md` for detailed migration workflows and `PGADMIN_SETUP.md` for database access.

More

See `DEV_GUIDE.md` and `POSTGRES_SETUP.md` for developer quickstarts and database guidance.
