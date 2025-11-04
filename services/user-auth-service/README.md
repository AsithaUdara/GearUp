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
GET http://localhost:8082/api/auth/health
```

## Database & Migrations

The user auth service uses PostgreSQL database `as_user_auth_service` with **Flyway** for schema management.

### Database Connection

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/as_user_auth_service
spring.datasource.username=svc_user_auth_service
spring.datasource.password=auth_svc_pass_2024
```

### Flyway Migrations

Migrations are located in `src/main/resources/db/migration/` and run automatically when the application starts.

**Run migrations manually:**

```powershell
# From repo root
.\mvnw.cmd -pl services/user-auth-service flyway:migrate

# Check migration status
.\mvnw.cmd -pl services/user-auth-service flyway:info

# Validate migrations
.\mvnw.cmd -pl services/user-auth-service flyway:validate
```

**Creating new migrations:**

1. Create a new file: `src/main/resources/db/migration/V{version}__{description}.sql`

   - Example: `V2__add_user_roles.sql`

2. Write idempotent SQL:

   ```sql
   ALTER TABLE users
   ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'USER';

   CREATE INDEX IF NOT EXISTS idx_users_role
   ON users(role);
   ```

3. Run the migration:
   ```powershell
   .\mvnw.cmd -pl services/user-auth-service flyway:migrate
   ```

**Database Tables:**

- `users` - User account information
- `user_sessions` - Active user sessions
- `flyway_schema_history` - Tracks migration history

See `FLYWAY_INTEGRATION.md` for detailed migration workflows and `PGADMIN_SETUP.md` for database access.

More

See `DEV_GUIDE.md` and `POSTGRES_SETUP.md` for developer quickstarts and database guidance.
