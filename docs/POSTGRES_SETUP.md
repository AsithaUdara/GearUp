# GearUp Backend - PostgreSQL Deployment Guide

## Architecture Overview

**Database-per-Service Pattern** with:

- Separate PostgreSQL databases for each microservice
- Dedicated database users with restricted access
- Connection pooling via HikariCP
- Automated initialization via Docker

### Services and Databases

| Service              | Database                  | User                       | Port |
| -------------------- | ------------------------- | -------------------------- | ---- |
| Template Service     | `as_template_service`     | `svc_template_service`     | 8085 |
| Notification Service | `as_notification_service` | `svc_notification_service` | 8081 |
| User Auth Service    | `as_user_auth_service`    | `svc_user_auth_service`    | 8082 |

## Quick Start

### Deploy Everything

```powershell
.\scripts\deploy.ps1
```

Options:

- `-Clean`: Remove all data and start fresh
- `-SkipTests`: Skip running tests
- `-SkipBuild`: Use existing JARs

### Manual Steps

1. Build: `.\mvnw.cmd clean package -DskipTests`
2. Start: `cd deployment\docker && docker-compose up -d`
3. Test: `.\scripts\test-db-connections.ps1`
4. Health: `.\scripts\health-check.ps1`

## Database Access

```powershell
# PostgreSQL CLI
docker exec -it gearup-postgres psql -U postgres

# Specific database
docker exec -it gearup-postgres psql -U postgres -d as_template_service

# List databases
docker exec -it gearup-postgres psql -U postgres -c "\l"
```

## Troubleshooting

```powershell
# View logs
cd deployment\docker
docker-compose logs -f db

# Test connections
.\scripts\test-db-connections.ps1

# Reset (WARNING: deletes data)
docker-compose down -v
docker-compose up -d
```

## Production Notes

1. Change default passwords in `.env`
2. Use managed PostgreSQL (RDS/Cloud SQL/Azure DB)
3. Enable SSL/TLS connections
4. Set up automated backups
5. Implement proper migration strategy (Flyway/Liquibase)
