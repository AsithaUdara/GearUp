# GearUp Backend - Complete Command Reference

## 📋 Verified Commands (Tested & Working)

### Building the Project

```powershell
# Build all modules (from project root)
.\mvnw clean install -DskipTests

# Build with tests
.\mvnw clean install

# Build specific service only
.\mvnw clean install -pl services/template-service -DskipTests
```

**Status**: ✅ TESTED - All 12 modules build successfully in ~21 seconds

---

### Starting Services

```powershell
# Start all services
cd deployment\docker
docker-compose up -d

# Start only database
docker-compose up -d db

# Start in foreground (see logs live)
docker-compose up

# Rebuild and start (after code changes)
docker-compose up -d --build
```

**Status**: ✅ TESTED - Services start (note: requires proper environment configuration)

---

### Stopping Services

```powershell
# Stop all services (from deployment/docker)
docker-compose down

# Stop and remove data volumes (CAUTION: Deletes all database data!)
docker-compose down -v

# Stop, remove volumes AND images (fresh clean start)
docker-compose down -v --rmi all
```

**Status**: ✅ TESTED - Clean shutdown works

---

### Viewing Logs

```powershell
# All services (live tail)
docker-compose logs -f

# Specific service (live tail)
docker-compose logs -f template-service

# Last 100 lines
docker-compose logs --tail=100 notification-service

# All logs since 10 minutes ago
docker-compose logs --since=10m

# Directly from Docker
docker logs gearup-postgres
docker logs gearup-template-service --tail 50
```

**Status**: ✅ TESTED - All logging commands work

---

### Checking Status

```powershell
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Check service status in Docker Compose
cd deployment\docker
docker-compose ps

# Check specific container health
docker inspect --format='{{.State.Health.Status}}' gearup-postgres
```

**Status**: ✅ TESTED

---

### Database Commands

```powershell
# Connect to PostgreSQL
docker exec -it gearup-postgres psql -U postgres

# List all databases
docker exec -it gearup-postgres psql -U postgres -c "\l"

# Connect to specific service database
docker exec -it gearup-postgres psql -U postgres -d as_automobile_service

# Check tables in database
docker exec -it gearup-postgres psql -U postgres -d as_template_service -c "\dt"

# Check Flyway migration history
docker exec -it gearup-postgres psql -U postgres -d as_template_service -c "SELECT * FROM flyway_schema_history;"

# Exit psql
\q
```

**Status**: ✅ TESTED - All databases (notification, user-auth, template, etc.) created successfully

---

### Testing

```powershell
# Run all tests (from project root)
.\mvnw test

# Run tests for specific module
.\mvnw test -pl services/template-service

# Run tests with code coverage
.\mvnw clean test jacoco:report

# Skip tests during build
.\mvnw clean install -DskipTests
```

**Status**: ⚠️ Tests exist but require services to be running

---

### Flyway Migrations

```powershell
# Migrations run AUTOMATICALLY when Spring Boot services start
# No manual command needed for normal operation

# To manually run Flyway migration (advanced)
cd services\template-service
..\..\mvnw flyway:migrate

# To check Flyway status
..\..\mvnw flyway:info

# To validate migrations
..\..\mvnw flyway:validate
```

**Status**: ✅ Flyway configured - Runs automatically on service startup

---

### Restart Individual Services

```powershell
# Restart single service
cd deployment\docker
docker-compose restart template-service

# Rebuild and restart single service
docker-compose up -d --build template-service

# Stop then start service
docker-compose stop template-service
docker-compose start template-service
```

**Status**: ✅ TESTED

---

### Cleaning Up

```powershell
# Remove stopped containers
docker container prune

# Remove unused images
docker image prune -a

# Remove unused volumes (CAUTION: Deletes data!)
docker volume prune

# Remove everything Docker (nuclear option)
docker system prune -a --volumes
```

**Status**: ✅ Standard Docker commands

---

### Development Workflow

#### 1. After Changing Java Code

```powershell
# From project root
.\mvnw clean install -DskipTests

# Restart services
cd deployment\docker
docker-compose up -d --build
```

#### 2. After Changing Database Schema

```powershell
# Create new migration file in:
# services/[service]/src/main/resources/db/migration/
# Name it: V2__description.sql (increment version number)

# Rebuild and restart
cd deployment\docker
docker-compose down
docker-compose up -d --build
```

#### 3. Complete Fresh Start

```powershell
# Stop and remove everything
cd deployment\docker
docker-compose down -v

# Rebuild code
cd ..\..
.\mvnw clean install -DskipTests

# Start fresh
cd deployment\docker
docker-compose up -d --build
```

**Status**: ✅ Standard workflow

---

### Troubleshooting Commands

```powershell
# Check Docker is running
docker --version
docker ps

# Check if port is in use
netstat -ano | findstr :8080

# Check service health
docker inspect --format='{{.State.Health.Status}}' gearup-postgres

# Enter running container
docker exec -it gearup-template-service /bin/sh

# Check environment variables in container
docker exec gearup-template-service env

# View real-time resource usage
docker stats

# Check network connections
docker network ls
docker network inspect docker_default
```

**Status**: ✅ Standard troubleshooting tools

---

## 🎯 Quick Reference Table

| Task              | Command                                            | Location            |
| ----------------- | -------------------------------------------------- | ------------------- |
| Build All         | `.\mvnw clean install -DskipTests`                 | Project root        |
| Start All         | `docker-compose up -d`                             | `deployment/docker` |
| Stop All          | `docker-compose down`                              | `deployment/docker` |
| View Logs         | `docker-compose logs -f`                           | `deployment/docker` |
| Check Status      | `docker ps`                                        | Anywhere            |
| Database Shell    | `docker exec -it gearup-postgres psql -U postgres` | Anywhere            |
| Rebuild & Restart | `docker-compose up -d --build`                     | `deployment/docker` |
| Clean Start       | `docker-compose down -v ; docker-compose up -d`    | `deployment/docker` |

---

## ✅ Verification Checklist

After starting services, verify everything is working:

- [ ] `docker ps` shows all containers running
- [ ] Database has 4 databases: `docker exec gearup-postgres psql -U postgres -c "\l"`
- [ ] Flyway tables exist (after services start): Check migration history
- [ ] Services are healthy: Check logs for "Started [Service]Application"
- [ ] No error logs: `docker-compose logs | findstr -i error`

---

## 📝 Notes

- **Environment Variables**: The docker-compose.yml uses hardcoded defaults for local development
- **Passwords**: Default passwords are for development only. Change for production!
- **Firebase**: Requires firebase-service-account.json file in project root
- **First Startup**: Takes 60-90 seconds for all services to fully initialize
- **Migrations**: Run automatically when services start, no manual intervention needed

---

**Last Updated**: 2024  
**Status**: Commands verified and tested  
**Build**: ✅ SUCCESS  
**Database**: ✅ 4 databases created  
**Docker**: ✅ Containers start
