# GearUp Backend - Quick Start Guide

**Welcome!** This guide will help you get the GearUp microservices system running on your computer, even if you're new to development.

## ✅ What's Been Verified

- ✅ Maven build completes successfully (all 12 modules)
- ✅ PostgreSQL database starts and creates all 4 databases
- ✅ Flyway migrations configured (run automatically when services start)
- ✅ Docker containers build and start
- ⚠️ **Note**: Firebase configuration needs to be set up (see Security section below)

---

## 📋 What You Need Before Starting

### 1. Install These Programs (One Time Setup)

- **Java 21** - [Download Here](https://www.oracle.com/java/technologies/downloads/#java21)

  - After installing, verify: Open PowerShell and type `java -version`
  - You should see version 21.x.x

- **Docker Desktop** - [Download Here](https://www.docker.com/products/docker-desktop/)

  - After installing, make sure Docker Desktop is running (you'll see a whale icon in your system tray)
  - Verify: Open PowerShell and type `docker --version`

- **Git** (Optional, if cloning from repository)
  - [Download Here](https://git-scm.com/downloads)

### 2. Get the Code

Either:

- **Option A**: Download ZIP from GitHub and extract it
- **Option B**: Clone using Git:
  ```powershell
  git clone https://github.com/AsithaUdara/GearUp-backend.git
  cd GearUp-backend
  ```

---

## 🚀 Quick Start (3 Simple Steps)

### Step 1: Build the Project

Open PowerShell in the project folder and run:

```powershell
.\mvnw clean install -DskipTests
```

**What this does**: Compiles all the Java code and creates executable JAR files.

**Wait time**: ~30 seconds

**Success looks like**:

```
[INFO] BUILD SUCCESS
[INFO] Total time:  21.391 s
```

✅ **VERIFIED**: Build completes successfully with all 12 modules

---

### Step 2: Update Docker Compose File (First Time Only)

The docker-compose.yml file needs default values to work properly. Edit `deployment/docker/docker-compose.yml` and ensure these environment variables have values instead of `${VAR_NAME}`:

**For PostgreSQL (db service)**:

```yaml
environment:
  POSTGRES_DB: postgres
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: 123456
  AUTOMOBILE_DB_PASSWORD: auto_svc_pass_2024
  NOTIFICATION_DB_PASSWORD: notif_svc_pass_2024
  USER_AUTH_DB_PASSWORD: auth_svc_pass_2024
  TEMPLATE_DB_PASSWORD: template_svc_pass_2024
```

**For Each Service** (automobile, notification, user-auth):

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/as_[SERVICE]_service
  - SPRING_DATASOURCE_USERNAME=svc_[SERVICE]_service
  - SPRING_DATASOURCE_PASSWORD=[SERVICE]_svc_pass_2024
```

**Note**: This step has been done for you if using the latest version.

---

### Step 3: Start All Services

```powershell
cd deployment\docker
docker-compose up -d
```

**What this does**: Starts all microservices and database.

**Wait time**: ~60 seconds for all services to fully start

**Check if everything is running**:

```powershell
docker ps
```

You should see 6+ containers running.

**Note**: Services may take 30-60 seconds to fully initialize. If services show as "starting", wait a bit longer.

---

## ✅ Verify It's Working

### Check Service Health

Open your web browser and visit these URLs:

1. **Service Discovery (Eureka)**: http://localhost:8761

   - You should see a dashboard with registered services

2. **API Gateway**: http://localhost:9090/actuator/health

   - You should see `{"status":"UP"}`

3. **Config Server**: http://localhost:8888/actuator/health
   - You should see `{"status":"UP"}`

### Check Database

1. **pgAdmin (Database Manager)**: http://localhost:5050
   - Email: `admin@gearup.com`
   - Password: `admin123`
2. After logging in:
   - Click "Add New Server"
   - General tab → Name: `GearUp Local`
   - Connection tab:
     - Host: `db`
     - Port: `5432`
     - Username: `postgres`
     - Password: `123456`
3. You should see 4 databases:
   - `as_automobile_service`
   - `as_notification_service`
   - `as_user_auth_service`
   - `as_template_service`

---

## 📊 Database Migrations (Flyway)

**Good news!** Migrations run automatically when services start.

### What Happens Automatically:

1. Service starts
2. Flyway checks for new migration files
3. Runs migrations in order (V1, V2, V3, etc.)
4. Service becomes ready

### Check Migration Status:

```powershell
# Connect to database container
docker exec -it gearup-postgres psql -U postgres -d as_automobile_service

# Check Flyway history
SELECT * FROM flyway_schema_history;

# Exit
\q
```

You should see entries showing which migrations ran and when.

---

## 🛠️ Common Commands

### View Logs

```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f automobile-service

# Last 100 lines
docker-compose logs --tail=100 notification-service
```

### Stop Everything

```powershell
docker-compose down
```

### Stop and Remove Everything (Fresh Start)

```powershell
# Stop and remove containers, networks, volumes
docker-compose down -v

# Also remove images
docker-compose down -v --rmi all
```

### Restart a Single Service

```powershell
docker-compose restart automobile-service
```

### Rebuild After Code Changes

```powershell
# Go back to project root
cd ..\..

# Rebuild the code
.\mvnw clean install -DskipTests

# Rebuild and restart Docker containers
cd deployment\docker
docker-compose up -d --build
```

---

## 🧪 Running Tests

### Run All Tests

```powershell
# From project root
.\mvnw test
```

### Run Tests for Specific Service

```powershell
.\mvnw test -pl services/automobile-service
```

### Run Tests with Coverage Report

```powershell
.\mvnw clean test jacoco:report
```

Coverage reports are saved in: `target/site/jacoco/index.html`

---

## 🐛 Troubleshooting

### Problem: "Docker is not running"

**Solution**:

1. Open Docker Desktop application
2. Wait for it to fully start (whale icon stops animating)
3. Try your command again

### Problem: "Port already in use"

**Solution**:

```powershell
# See what's using the port
netstat -ano | findstr :8080

# Stop the service using that port, or:
docker-compose down
```

### Problem: "Cannot connect to database"

**Solutions**:

1. Check if database is running:

   ```powershell
   docker ps | findstr postgres
   ```

2. Check database logs:

   ```powershell
   docker-compose logs db
   ```

3. Restart database:
   ```powershell
   docker-compose restart db
   ```

### Problem: "Service won't start"

**Solutions**:

1. Check logs:

   ```powershell
   docker-compose logs <service-name>
   ```

2. Common issues:
   - **Missing .env file**: Make sure `.env` exists in project root
   - **Wrong environment variables**: Check `.env` file has all required values
   - **Database not ready**: Wait 30 seconds after starting database before starting services

### Problem: "Migrations failed"

**Solutions**:

1. Check Flyway logs in service logs:

   ```powershell
   docker-compose logs automobile-service | findstr -i flyway
   ```

2. Reset database (WARNING: Deletes all data):

   ```powershell
   docker-compose down -v
   docker-compose up -d
   ```

3. Manually run migrations:

   ```powershell
   # Load environment variables from .env (PowerShell)
   Get-Content ..\..\. env | ForEach-Object {
       if ($_ -match '^([^#][^=]+)=(.*)$') {
           [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
       }
   }

   # Run Flyway for specific service
   cd ..\..\services\automobile-service
   ..\..\mvnw flyway:migrate
   ```

---

## 📁 Project Structure

```
GearUp-backend/
├── services/                    # Microservices
│   ├── automobile-service/      # Vehicle management (Port 8080)
│   ├── notification-service/    # Notifications (Port 8081)
│   ├── user-auth-service/       # Authentication (Port 8082)
│   └── template-service/        # Template service (Port 8083)
├── api-gateway/                 # API Gateway (Port 9090)
├── service-discovery/           # Eureka Server (Port 8761)
├── config-server/               # Config Server (Port 8888)
├── shared-libs/                 # Shared code libraries
│   ├── security-lib/            # Firebase auth
│   ├── common-dto/              # Data transfer objects
│   ├── common-utils/            # Utility functions
│   └── event-models/            # Event definitions
├── deployment/                  # Deployment files
│   ├── docker/                  # Docker Compose
│   │   └── docker-compose.yml   # Main deployment file
│   └── postgres/                # Database initialization
│       └── init-db.sql          # Creates databases & users
├── .env                         # Environment variables (DO NOT COMMIT!)
├── pom.xml                      # Main Maven configuration
└── mvnw (& mvnw.cmd)           # Maven wrapper (no installation needed)
```

---

## 🎯 Service Ports Reference

| Service                    | Port  | URL                    | Purpose                         |
| -------------------------- | ----- | ---------------------- | ------------------------------- |
| Service Discovery (Eureka) | 8761  | http://localhost:8761  | See all registered services     |
| API Gateway                | 9090  | http://localhost:9090  | Single entry point for all APIs |
| Config Server              | 8888  | http://localhost:8888  | Centralized configuration       |
| Automobile Service         | 8080  | http://localhost:8080  | Vehicle management              |
| Notification Service       | 8081  | http://localhost:8081  | Send notifications              |
| User Auth Service          | 8082  | http://localhost:8082  | User authentication             |
| Template Service           | 8083  | http://localhost:8083  | Template operations             |
| PostgreSQL                 | 5432  | localhost:5432         | Database                        |
| pgAdmin                    | 5050  | http://localhost:5050  | Database management UI          |
| RabbitMQ Management        | 15672 | http://localhost:15672 | Message queue UI                |
| Redis                      | 6379  | localhost:6379         | Cache                           |

---

## 🔐 Security Notes

### Important Files (Never Commit These!)

- `.env` - Contains passwords and secrets
- `firebase-service-account.json` - Firebase credentials

These files are already in `.gitignore` so they won't be committed to Git.

### Default Passwords (Change for Production!)

**PostgreSQL**:

- Admin User: `postgres` / Password: `123456`
- Service users: See `.env` file

**RabbitMQ**:

- User: `automobile_admin` / Password: `123456`

**pgAdmin**:

- Email: `admin@gearup.com` / Password: `admin123`

---

## 📚 Next Steps

1. **Learn More**:

   - [Security Hardening Guide](docs/SECURITY_HARDENING.md)
   - [Flyway Migration Guide](docs/FLYWAY_INTEGRATION.md)
   - [Development Guide](docs/DEV_GUIDE.md)

2. **Add a New Service**:

   - See main README.md "Adding a New Microservice" section

3. **Deploy to Production**:
   - See [Deployment Guide](deployment/README.md)

---

## 💬 Need Help?

1. **Check logs**: `docker-compose logs -f <service-name>`
2. **Check this guide's Troubleshooting section**
3. **Read detailed docs**: `docs/` folder
4. **Ask your team lead**

---

## 📝 Quick Command Cheat Sheet

```powershell
# BUILD
.\mvnw clean install -DskipTests          # Build all services

# START
cd deployment\docker
docker-compose up -d                      # Start everything
docker-compose up -d db                   # Start only database

# STOP
docker-compose down                       # Stop all services
docker-compose down -v                    # Stop and delete data
docker-compose down -v --rmi all          # Stop, delete data AND images

# CHECK STATUS
docker ps                                 # See running containers
docker-compose logs -f                    # See all logs (live)
docker-compose logs --tail=100 <service>  # Last 100 log lines

# RESTART
docker-compose restart <service>          # Restart one service
docker-compose up -d --build              # Rebuild and restart all

# TEST
.\mvnw test                               # Run all tests
.\mvnw test -pl services/<service-name>   # Test one service

# DATABASE
docker exec -it gearup-postgres psql -U postgres -d as_automobile_service
# Then: SELECT * FROM flyway_schema_history; to see migrations
```

---

**That's it! You're ready to develop on GearUp Backend! 🚀**
