# 🚀 Setup Guide: Connect Docker Desktop with GearUp Backend

## ✅ Prerequisites Check

- ✅ Java 21 - Installed
- ✅ Maven Wrapper - Available
- ✅ Docker Desktop - Running
- ⚠️ Firebase JSON - Need to configure

---

## Step 1: Configure Environment File (.env)

### 1.1 Update Database Configurations

Open `.env` file and add/update these configurations:

```env
# Database (local development defaults)
POSTGRES_DB=postgres
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Automobile Service Database
AUTOMOBILE_DB_URL=jdbc:postgresql://db:5432/as_automobile_service
AUTOMOBILE_DB_USER=svc_automobile_service
AUTOMOBILE_DB_PASSWORD=auto_secure_pass_2024

# Notification Service Database
NOTIFICATION_DB_URL=jdbc:postgresql://db:5432/as_notification_service
NOTIFICATION_DB_USER=svc_notification_service
NOTIFICATION_DB_PASSWORD=notif_secure_pass_2024

# User Auth Service Database
USER_AUTH_DB_URL=jdbc:postgresql://db:5432/as_user_auth_service
USER_AUTH_DB_USER=svc_user_auth_service
USER_AUTH_DB_PASSWORD=auth_secure_pass_2024

# Template Service Database
TEMPLATE_DB_URL=jdbc:postgresql://db:5432/as_template_service
TEMPLATE_DB_USER=svc_template_service
TEMPLATE_DB_PASSWORD=template_secure_pass_2024
```

### 1.2 Configure Firebase (Optional for now, can add later)

1. **Get Firebase Service Account JSON:**
   - Go to: https://console.firebase.google.com/
   - Select your project → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Save the JSON file (e.g., `firebase-service-account.json`)

2. **Add to .env:**
   ```env
   FIREBASE_CREDENTIALS_HOST_PATH=E:\software project\EAD\GearUp-backend\firebase-service-account.json
   ```
   *(Use the actual path where you saved the file)*

   **Note:** If you don't have Firebase yet, you can leave this empty and add it later. The database will work without Firebase.

---

## Step 2: Build the Project

Build all services (this compiles Java code):

```powershell
.\mvnw.cmd clean install -DskipTests
```

This will:
- Compile all services
- Package them into JAR files
- Create Docker images later

---

## Step 3: Start PostgreSQL Database with Docker

Start only the database first to test the connection:

```powershell
# Navigate to docker folder
cd deployment\docker

# Start PostgreSQL database
docker compose up -d db

# Check if database is running
docker ps
```

**Expected Output:**
You should see a container named `gearup-postgres` running on port 5432.

---

## Step 4: Verify Database Connection

Test if you can connect to the database:

```powershell
# Go back to project root
cd ..\..

# Check database logs
docker logs gearup-postgres

# Test connection (if you have psql installed)
docker exec -it gearup-postgres psql -U postgres -d postgres
```

---

## Step 5: Start All Infrastructure Services

For developing a single service, you need:

1. **PostgreSQL Database** ✅
2. **Service Discovery (Eureka)** - Optional for single service
3. **Config Server** - Optional for single service

### Option A: Start Only Database (Simplest for single service)

```powershell
cd deployment\docker
docker compose up -d db
```

### Option B: Start All Infrastructure Services

```powershell
cd deployment\docker
docker compose up -d db service-discovery config-server
```

### Option C: Start Everything (All Services)

```powershell
cd deployment\docker
docker compose up --build -d
```

---

## Step 6: Verify Docker Connection

### Check Running Containers

```powershell
docker ps
```

You should see containers running.

### View Logs

```powershell
# Database logs
docker logs gearup-postgres

# All services logs
docker compose -f deployment/docker/docker-compose.yml logs -f
```

### Stop Services

```powershell
cd deployment\docker
docker compose down
```

---

## Step 7: Connect Your Service to Database

When running your service locally (not in Docker), use these connection strings:

### For Notification Service:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/as_notification_service
spring.datasource.username=svc_notification_service
spring.datasource.password=notif_secure_pass_2024
```

### For Automobile Service:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/as_automobile_service
spring.datasource.username=svc_automobile_service
spring.datasource.password=auto_secure_pass_2024
```

**Note:** When services run inside Docker containers, use `db:5432` instead of `localhost:5432`.

---

## Quick Commands Reference

```powershell
# Build project
.\mvnw.cmd clean install -DskipTests

# Start database only
cd deployment\docker
docker compose up -d db

# Start all services
docker compose -f deployment/docker/docker-compose.yml up --build -d

# View logs
docker compose -f deployment/docker/docker-compose.yml logs -f

# Stop all services
docker compose -f deployment/docker/docker-compose.yml down

# Check containers
docker ps

# Check database connection
docker exec -it gearup-postgres psql -U postgres
```

---

## Troubleshooting

### Docker Desktop not starting containers
- Make sure Docker Desktop is running (check system tray)
- Check Docker Desktop settings → Resources → ensure enough memory allocated

### Port 5432 already in use
- Stop any local PostgreSQL installations
- Or change port in docker-compose.yml

### Can't connect to database
- Verify database container is running: `docker ps`
- Check database logs: `docker logs gearup-postgres`
- Verify credentials in `.env` file

### Firebase errors (if Firebase not configured yet)
- You can run services without Firebase initially
- Set `FIREBASE_CREDENTIALS_HOST_PATH=` (empty) if not using Firebase yet

---

## Next Steps

1. ✅ Configure `.env` file with database credentials
2. ✅ Start PostgreSQL: `cd deployment\docker && docker compose up -d db`
3. ✅ Build project: `.\mvnw.cmd clean install -DskipTests`
4. ✅ Test database connection
5. 🚀 Start coding your service!
