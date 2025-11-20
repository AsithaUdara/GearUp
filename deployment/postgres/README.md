# PostgreSQL Database Configuration

This folder contains PostgreSQL setup for the GearUp microservices.

## Files

- **init-db.sql**: Database initialization script (creates databases, users, tables, and indexes)
- **secret-postgres.yml**: Kubernetes secret template for PostgreSQL credentials
- **statefulset-postgres.yml**: Kubernetes StatefulSet template for PostgreSQL deployment

## Database Architecture

Following the **Database-per-Service** pattern:

| Service      | Database                  | User                       | Password (Env Var)         |
| ------------ | ------------------------- | -------------------------- | -------------------------- |
| Template     | `as_template_service`     | `svc_template_service`     | `TEMPLATE_DB_PASSWORD`     |
| Notification | `as_notification_service` | `svc_notification_service` | `NOTIFICATION_DB_PASSWORD` |
| User Auth    | `as_user_auth_service`    | `svc_user_auth_service`    | `USER_AUTH_DB_PASSWORD`    |

## Local Development

For local development, use Docker Compose:

```powershell
cd deployment/docker
docker-compose up -d
```

The `init-db.sql` script runs automatically on first startup.

## Production Deployment

**Recommended**: Use managed PostgreSQL services:

- AWS RDS
- Azure Database for PostgreSQL
- Google Cloud SQL

For self-hosted Kubernetes deployment:

1. Update `secret-postgres.yml` with your credentials
2. Adjust `statefulset-postgres.yml` for your cluster (storage class, resources, etc.)
3. Apply manifests: `kubectl apply -f secret-postgres.yml -f statefulset-postgres.yml`

## Database Initialization

The `init-db.sql` script:

- Creates separate databases for each microservice
- Creates dedicated database users with restricted permissions
- Creates initial table schemas with proper indexes
- Sets default privileges for future tables

## Connection Details

Services connect using environment variables from `.env`:

```properties
TEMPLATE_DB_URL=jdbc:postgresql://db:5432/as_template_service
TEMPLATE_DB_USER=svc_template_service
TEMPLATE_DB_PASSWORD=template_svc_pass_2024
```

See root `POSTGRES_SETUP.md` for complete setup instructions.
