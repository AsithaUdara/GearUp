# pgAdmin Setup Guide

This guide explains how to connect pgAdmin to view and manage the GearUp PostgreSQL databases.

## Quick Start

### Option 1: Using pgAdmin Docker Container (Recommended)

1. **Add pgAdmin to Docker Compose**

   Add this service to `deployment/docker/docker-compose.yml`:

   ```yaml
   pgadmin:
     image: dpage/pgadmin4:latest
     container_name: gearup-pgadmin
     environment:
       PGADMIN_DEFAULT_EMAIL: admin@gearup.com
       PGADMIN_DEFAULT_PASSWORD: admin123
       PGADMIN_CONFIG_SERVER_MODE: "False"
     ports:
       - "5050:80"
     depends_on:
       - db
     restart: unless-stopped
   ```

2. **Start pgAdmin**

   ```powershell
   docker compose -f deployment/docker/docker-compose.yml up -d pgadmin
   ```

3. **Access pgAdmin**

   Open your browser and go to: `http://localhost:5050`

   Login credentials:

   - Email: `admin@gearup.com`
   - Password: `admin123`

### Option 2: Using Desktop pgAdmin

1. **Download & Install pgAdmin**

   Download from: https://www.pgadmin.org/download/

2. **Add Server Connection**

   Right-click "Servers" → "Register" → "Server"

## Database Connection Details

### General Tab

- **Name**: GearUp Backend (or any name you prefer)

### Connection Tab

| Field                | Value                                         |
| -------------------- | --------------------------------------------- |
| Host name/address    | `localhost` (or `db` if using Docker pgAdmin) |
| Port                 | `5432`                                        |
| Maintenance database | `postgres`                                    |
| Username             | `postgres`                                    |
| Password             | `123456` (default from docker-compose.yml)    |

## Service Databases

Once connected, you'll see these databases:

| Database Name             | Service              | User                       | Password                 |
| ------------------------- | -------------------- | -------------------------- | ------------------------ |
| `as_automobile_service`   | Automobile Service   | `svc_automobile_service`   | `auto_svc_pass_2024`     |
| `as_notification_service` | Notification Service | `svc_notification_service` | `notif_svc_pass_2024`    |
| `as_user_auth_service`    | User Auth Service    | `svc_user_auth_service`    | `auth_svc_pass_2024`     |
| `as_template_service`     | Template Service     | `svc_template_service`     | `template_svc_pass_2024` |

## Viewing Tables and Data

1. **Expand Database**

   - Servers → GearUp Backend → Databases → [database_name]

2. **View Tables**

   - Schemas → public → Tables

3. **View Data**

   - Right-click on table → View/Edit Data → All Rows

4. **Run SQL Queries**
   - Right-click on database → Query Tool
   - Write your SQL and press F5 or click Execute

## Common SQL Queries

### Check Flyway Migration History

```sql
-- Connect to any service database first
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### View All Tables in Current Database

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

### Automobile Service Queries

```sql
-- Connect to as_automobile_service

-- View all vehicles
SELECT * FROM vehicles ORDER BY created_at DESC LIMIT 10;

-- Count vehicles by status
SELECT status, COUNT(*)
FROM vehicles
GROUP BY status;
```

### Notification Service Queries

```sql
-- Connect to as_notification_service

-- View recent notifications
SELECT * FROM notifications
ORDER BY created_at DESC
LIMIT 10;

-- Count notifications by type
SELECT notification_type, COUNT(*)
FROM notifications
GROUP BY notification_type;
```

### User Auth Service Queries

```sql
-- Connect to as_user_auth_service

-- View all users
SELECT id, email, first_name, last_name, created_at
FROM users
ORDER BY created_at DESC;

-- View active sessions
SELECT us.*, u.email
FROM user_sessions us
JOIN users u ON us.user_id = u.id
WHERE us.is_active = true;
```

## Troubleshooting

### Cannot Connect to PostgreSQL

**Problem**: Connection refused or timeout

**Solution**:

1. Ensure PostgreSQL container is running:

   ```powershell
   docker ps | findstr postgres
   ```

2. Check if port 5432 is exposed:

   ```powershell
   docker port gearup-postgres
   ```

3. Verify container logs:
   ```powershell
   docker logs gearup-postgres --tail 50
   ```

### Authentication Failed

**Problem**: Password authentication failed

**Solution**:

1. For postgres superuser, use password from docker-compose.yml (default: `123456`)
2. For service users, use passwords from `deployment/postgres/init-db.sql`
3. If using Docker pgAdmin, use hostname `db` instead of `localhost`

### Tables Not Visible

**Problem**: Database exists but no tables shown

**Solution**:

1. Check if Flyway migrations have run:

   ```sql
   SELECT EXISTS (
       SELECT FROM information_schema.tables
       WHERE table_schema = 'public'
       AND table_name = 'flyway_schema_history'
   );
   ```

2. If `flyway_schema_history` doesn't exist, migrations haven't run yet
3. Start the Spring Boot service or run migrations manually:
   ```powershell
   .\mvnw.cmd -pl services/automobile-service flyway:migrate
   ```

## Security Notes

### Production Environment

**⚠️ NEVER use these default credentials in production!**

For production:

1. Change all default passwords
2. Use environment variables for credentials
3. Enable SSL/TLS for PostgreSQL connections
4. Restrict pgAdmin access with strong authentication
5. Use secrets management (e.g., Azure Key Vault, AWS Secrets Manager)

### Development Best Practices

1. **Never commit credentials** to version control
2. Use `.env` files (add to `.gitignore`)
3. Rotate passwords periodically
4. Limit database user permissions to what's needed

## Advanced Configuration

### Using pgAdmin with Docker Internal Network

If pgAdmin and PostgreSQL are in the same Docker network:

**Connection Tab**:

- Host: `db` (Docker service name)
- Port: `5432`
- Username: `postgres`
- Password: `123456`

### SSH Tunnel Configuration

For remote database access:

**SSH Tunnel Tab**:

- Use SSH tunneling: `Yes`
- Tunnel host: `your-server-ip`
- Tunnel port: `22`
- Username: `your-ssh-user`
- Authentication: SSH key or password

## Backup and Restore via pgAdmin

### Backup a Database

1. Right-click on database → Backup
2. Choose format (Custom recommended)
3. Select filename and location
4. Click "Backup"

### Restore a Database

1. Right-click on database → Restore
2. Select backup file
3. Review options
4. Click "Restore"

## Useful pgAdmin Features

### Query History

- Tools → Query History (View previously executed queries)

### ERD (Entity Relationship Diagram)

- Right-click schema → ERD For Schema
- Visualize table relationships

### Dashboard

- Click on database to see:
  - Server activity
  - Database statistics
  - Session activity

### Import/Export Data

- Right-click table → Import/Export Data
- Supports CSV, TEXT formats

## Environment Variables Reference

Create a `.env` file in the project root:

```env
# PostgreSQL Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=postgres
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123456

# pgAdmin Configuration
PGADMIN_EMAIL=admin@gearup.com
PGADMIN_PASSWORD=admin123
```

## Resources

- [pgAdmin Documentation](https://www.pgadmin.org/docs/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Hub - pgAdmin4](https://hub.docker.com/r/dpage/pgadmin4)
- [Flyway Documentation](https://documentation.red-gate.com/flyway)

---

_Last Updated: November 5, 2025_  
_Version: 1.0_
