Template Service — skeleton

This folder is a minimal skeleton you can copy when creating a new microservice.

How to use

1. Copy this folder and rename it to `services/my-service`.
2. Edit `pom.xml`: change `artifactId`, `name`, and add dependencies your service needs.
3. Replace the package `com.gearup.templateservice` and class name with your service name.
4. Add `application.properties` under `src/main/resources` with service configuration including database and Flyway settings.
5. Create initial migration file: `src/main/resources/db/migration/V1__initial_schema.sql`
6. Add the new module to the root reactor if your root POM lists modules explicitly.
7. Add a config YAML to `config-repo/` (optional) so Config Server can serve settings.
8. Add database creation to `deployment/postgres/init-db.sql`.

Quick build & run (from repo root):

```powershell
.\mvnw.cmd -pl services/my-service -am -DskipTests package
docker compose -f deployment/docker/docker-compose.yml build my-service --progress=plain
docker compose -f deployment/docker/docker-compose.yml up --build -d my-service

# Follow logs
docker compose -f deployment/docker/docker-compose.yml logs -f my-service
```

## Database & Migrations

All new services should use **Flyway** for database schema management.

### Setup Database for New Service

1. **Add database creation to `deployment/postgres/init-db.sql`:**

   ```sql
   -- Create database
   CREATE DATABASE as_my_service;

   -- Create service user
   CREATE USER svc_my_service WITH PASSWORD 'my_service_pass_2024';

   -- Grant privileges
   GRANT ALL PRIVILEGES ON DATABASE as_my_service TO svc_my_service;

   \c as_my_service;
   GRANT ALL ON SCHEMA public TO svc_my_service;
   ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO svc_my_service;
   ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO svc_my_service;
   ```

2. **Configure Flyway in `pom.xml`:**

   ```xml
   <dependencies>
       <dependency>
           <groupId>org.flywaydb</groupId>
           <artifactId>flyway-core</artifactId>
       </dependency>
       <dependency>
           <groupId>org.flywaydb</groupId>
           <artifactId>flyway-database-postgresql</artifactId>
           <scope>runtime</scope>
       </dependency>
   </dependencies>

   <build>
       <plugins>
           <plugin>
               <groupId>org.flywaydb</groupId>
               <artifactId>flyway-maven-plugin</artifactId>
           </plugin>
       </plugins>
   </build>
   ```

3. **Add Flyway configuration to `application.properties`:**

   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/as_my_service
   spring.datasource.username=svc_my_service
   spring.datasource.password=my_service_pass_2024

   # Flyway Configuration
   spring.flyway.enabled=true
   spring.flyway.baseline-on-migrate=true
   spring.flyway.locations=classpath:db/migration
   spring.flyway.validate-on-migrate=true
   spring.flyway.out-of-order=false
   ```

4. **Create initial migration:**

   Create file: `src/main/resources/db/migration/V1__initial_schema.sql`

   ```sql
   -- V1__initial_schema.sql
   -- Initial schema for my-service

   CREATE TABLE IF NOT EXISTS my_table (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       name VARCHAR(255) NOT NULL,
       description TEXT,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );

   CREATE INDEX IF NOT EXISTS idx_my_table_name ON my_table(name);

   COMMENT ON TABLE my_table IS 'Main data table for my-service';
   ```

5. **Run migrations:**

   ```powershell
   .\mvnw.cmd -pl services/my-service flyway:migrate
   ```

See `FLYWAY_INTEGRATION.md` for detailed Flyway workflows and `PGADMIN_SETUP.md` for database access.

Notes:

- Use the existing services as references for logging, config, and Dockerfile patterns.
- Do not commit secrets. Use `.env` or an external secrets manager.
- Always write idempotent migrations using `IF EXISTS` / `IF NOT EXISTS` clauses.
- Follow the migration naming convention: `V{version}__{description}.sql`
