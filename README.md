# GearUp Backend - Microservices Infrastructure

Production-ready Spring Boot microservices architecture with service discovery, centralized configuration, API gateway, and PostgreSQL database-per-service pattern.

## 🚀 **NEW DEVELOPERS START HERE!**

**👉 Read the [Quick Start Guide](QUICK_START.md) for step-by-step beginner-friendly instructions**

**👉 See [COMMANDS.md](COMMANDS.md) for complete verified command reference**

### Quick Start (3 Steps):

1. `.\mvnw clean install -DskipTests` - Build the project
2. `cd deployment\docker` - Go to docker folder
3. `docker-compose up -d` - Start all services

That's it! See [QUICK_START.md](QUICK_START.md) for details.

---

## 🏗️ Architecture Overview

```
GearUp Backend
├── Infrastructure Services
│   ├── api-gateway/           # Spring Cloud Gateway (Port 9090)
│   ├── service-discovery/     # Eureka Server (Port 8761)
│   └── config-server/         # Spring Cloud Config (Port 8888)
├── Business Services
│   ├── automobile-service/    # Vehicle management (Port 8080)
│   ├── notification-service/  # Notifications & alerts (Port 8081)
│   ├── user-auth-service/     # Authentication (Port 8082)
│   └── template-service/      # Template service (Port 8083)
├── Shared Libraries
│   ├── security-lib/          # Firebase auth utilities
│   ├── common-dto/            # Shared data models
│   ├── common-utils/          # Utility classes
│   └── event-models/          # Event-driven patterns
└── Infrastructure
    ├── deployment/            # Docker & PostgreSQL
    ├── k8s/                   # Kubernetes manifests
    └── config-repo/           # Centralized configs
```

---

## 📚 Documentation

### Getting Started

- **[Quick Start Guide](docs/QUICK_START.md)** - Get up and running in minutes
- **[Commands Reference](docs/COMMANDS.md)** - All verified commands
- **[Development Guide](docs/DEV_GUIDE.md)** - Development workflows

### Cross-Service Communication 🆕

- **[Cross-Service Communication Guide](CROSS_SERVICE_COMMUNICATION.md)** - Complete guide for microservice communication
- **[RabbitMQ Setup & Usage](RABBITMQ_GUIDE.md)** - Event-driven messaging patterns
- **Topics Covered:**
  - ✅ Asynchronous messaging with RabbitMQ
  - ✅ Synchronous communication with Feign + Eureka
  - ✅ API Gateway routing patterns
  - ✅ Service discovery and load balancing
  - ✅ Event publishing and consumption
  - ✅ Error handling and resilience patterns

### Infrastructure

- **[PostgreSQL Setup](docs/POSTGRES_SETUP.md)** - Database configuration
- **[PgAdmin Setup](docs/PGADMIN_SETUP.md)** - Database management UI
- **[Infrastructure Status](docs/INFRASTRUCTURE_STATUS.md)** - Service health
- **[Backup & Restore](docs/backup-and-restore.md)** - Data management

### For Beginners

- **[Quick Start Guide](QUICK_START.md)** - Step-by-step setup for new developers
- **[Complete Command Reference](COMMANDS.md)** - All verified commands with examples
- **[Security Quick Reference](docs/SECURITY_QUICK_REFERENCE.md)** - Security basics

### For Advanced Users

- **[Development Guide](docs/DEV_GUIDE.md)** - Detailed development workflows
- **[Flyway Integration](docs/FLYWAY_INTEGRATION.md)** - Database migration guide
- **[Security Hardening](docs/SECURITY_HARDENING.md)** - Complete security documentation
- **[Infrastructure Status](docs/INFRASTRUCTURE_STATUS.md)** - System architecture details

---

## ✨ Key Features

### Infrastructure

- **Service Discovery**: Eureka-based auto-discovery and load balancing
- **API Gateway**: Centralized routing, authentication, and rate limiting
- **Config Server**: Git-backed configuration management
- **Database**: PostgreSQL 15 with database-per-service pattern
- **Security**: Firebase Authentication integration
- **Caching**: Caffeine cache for rate limiting
- **Health Checks**: Comprehensive health monitoring

### Development

- **Multi-module Maven**: Centralized dependency management
- **Docker Compose**: One-command local deployment
- **Automated Scripts**: Deploy, test, and verify scripts
- **Shared Libraries**: Reusable components across services

## 🚀 Quick Start (30 seconds)

### Prerequisites

- Java 21
- Docker Desktop
- Maven (or use included Maven Wrapper)

### 1. Environment Setup

```powershell
# Copy environment template
Copy-Item .env.example .env

# Edit .env with your Firebase credentials
notepad .env
```

### 2. Deploy Everything

```powershell
# Builds all services, starts PostgreSQL + all microservices
.\scripts\deploy.ps1
```

### 3. Verify Deployment

```powershell
# Check all services health
.\scripts\health-check.ps1

# Test database connections
.\scripts\test-db-connections.ps1
```

### 4. Access Services

- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **Notification Service**: http://localhost:8081
- **Automobile Service**: http://localhost:8082

## 📋 Requirements

- **Java**: 21 (LTS)
- **Maven**: 3.8+ (or use `mvnw.cmd`)
- **Docker**: 24.0+ with Docker Compose
- **PostgreSQL**: 15-alpine (via Docker)
- **Firebase**: Service account JSON for authentication

## 🏗️ For Teammates: Building New Services

### Service Template Structure

```
your-service/
├── src/
│   ├── main/
│   │   ├── java/com/gearup/yourservice/
│   │   │   ├── YourServiceApplication.java  # @EnableDiscoveryClient
│   │   │   ├── controller/                  # REST endpoints
│   │   │   ├── service/                     # Business logic
│   │   │   ├── repository/                  # Data access
│   │   │   ├── model/                       # JPA entities
│   │   │   ├── dto/                         # Data transfer objects
│   │   │   └── config/                      # Service configuration
│   │   └── resources/
│   │       └── application.properties       # Service config
│   └── test/
│       └── java/                            # Unit tests
├── pom.xml                                  # Dependencies
└── Dockerfile                               # Container build
```

### Step-by-Step: Adding a New Service

#### 1. Create Service Module

```powershell
# Create directory structure
mkdir services/your-service/src/main/java/com/gearup/yourservice
mkdir services/your-service/src/main/resources
mkdir services/your-service/src/test/java
```

#### 2. Create pom.xml

```xml
<project>
    <parent>
        <groupId>com.gearup</groupId>
        <artifactId>gearup-root</artifactId>
        <version>1.0.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>your-service</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Service Discovery -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- Config Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>

        <!-- Shared Libraries -->
        <dependency>
            <groupId>com.gearup</groupId>
            <artifactId>shared-security-lib</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.gearup</groupId>
            <artifactId>shared-common-utils</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</project>
```

#### 3. Create Application Class

```java
package com.gearup.yourservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

#### 4. Configure application.properties

```properties
# Service Identity
spring.application.name=your-service
server.port=8084

# Config Server
spring.config.import=optional:configserver:http://localhost:8888
spring.cloud.config.name=your-service

# Database
spring.datasource.url=${YOUR_SERVICE_DB_URL}
spring.datasource.username=${YOUR_SERVICE_DB_USER}
spring.datasource.password=${YOUR_SERVICE_DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false

# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

#### 5. Add Database to PostgreSQL and Create Flyway Migrations

Edit `deployment/postgres/init-db.sql` to add database and user (NOT tables):

```sql
-- Create database
CREATE DATABASE as_your_service;

-- Create user
CREATE USER your_service_user WITH PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE as_your_service TO your_service_user;

-- Connect and grant schema permissions
\c as_your_service;
GRANT ALL ON SCHEMA public TO your_service_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO your_service_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO your_service_user;
```

Create Flyway migration at `services/your-service/src/main/resources/db/migration/V1__initial_schema.sql`:

```sql
-- V1__initial_schema.sql
-- Creates initial schema for your-service

CREATE TABLE your_table (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_your_table_name ON your_table(name);

-- Add table comments
COMMENT ON TABLE your_table IS 'Stores your business entities';
```

Add Flyway configuration to `services/your-service/src/main/resources/application.properties`:

```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true
spring.flyway.out-of-order=false
```

#### 6. Add to Docker Compose

Edit `deployment/docker/docker-compose.yml`:

```yaml
your-service:
  build:
    context: ../../
    dockerfile: ./services/your-service/Dockerfile
  container_name: gearup-your-service
  ports:
    - "8084:8084"
  env_file:
    - ../../.env
  environment:
    - SPRING_DATASOURCE_URL=${YOUR_SERVICE_DB_URL}
    - SPRING_DATASOURCE_USERNAME=${YOUR_SERVICE_DB_USER}
    - SPRING_DATASOURCE_PASSWORD=${YOUR_SERVICE_DB_PASSWORD}
  depends_on:
    db:
      condition: service_healthy
    service-discovery:
      condition: service_started
  restart: unless-stopped
```

#### 7. Create Dockerfile

Copy from `services/notification-service/Dockerfile` and adjust:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY services/your-service/target/your-service-1.0.0.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 8. Add to API Gateway Routes

Edit `api-gateway/src/main/resources/application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: your-service
          uri: lb://your-service
          predicates:
            - Path=/api/your-service/**
          filters:
            - StripPrefix=2
```

#### 9. Create Config in config-repo

Create `config-repo/your-service.yml`:

```yaml
server:
  port: 8084

spring:
  application:
    name: your-service

# Service-specific configurations
your-service:
  feature:
    enabled: true
```

#### 10. Add to Root pom.xml

Edit root `pom.xml` modules section:

```xml
<modules>
    <!-- ... existing modules ... -->
    <module>services/your-service</module>
</modules>
```

#### 11. Build and Deploy

```powershell
# Build your service
.\mvnw.cmd clean package -pl services/your-service -am

# Deploy with Docker
docker compose -f deployment/docker/docker-compose.yml up -d your-service

# Check logs
docker logs -f gearup-your-service
```

### Testing Your Service

```powershell
# Health check
curl http://localhost:8084/actuator/health

# Via API Gateway
curl http://localhost:8080/api/your-service/your-endpoint

# Check Eureka registration
# Visit http://localhost:8761 - your service should appear
```

## 📚 Module Documentation

### Infrastructure Services

#### api-gateway/

**Purpose**: Single entry point for all client requests

- **Port**: 8080
- **Features**:
  - Firebase authentication
  - Route management
  - Rate limiting (Caffeine cache)
  - Load balancing
  - Circuit breaker ready
- **Key Files**:
  - `application.yml`: Route configuration
  - `FirebaseGatewayFilter.java`: Auth filter
  - `RateLimitConfig.java`: Rate limiting setup

#### service-discovery/

**Purpose**: Service registration and discovery (Eureka)

- **Port**: 8761
- **Features**:
  - Automatic service registration
  - Health monitoring
  - Client-side load balancing
  - Service catalog
- **Dashboard**: http://localhost:8761
- **Usage**: All services auto-register on startup

#### config-server/

**Purpose**: Centralized configuration management

- **Port**: 8888
- **Features**:
  - Git-backed configurations
  - Environment-specific profiles
  - Encryption support
  - Dynamic config refresh
- **Config Location**: `config-repo/`
- **Test**: http://localhost:8888/notification-service/default

### Shared Libraries

#### shared-libs/security-lib/

**Purpose**: Firebase authentication utilities

- `FirebaseAuthService.java`: Token verification
- `FirebaseConfig.java`: SDK initialization
- Used by: API Gateway, all secured services

#### shared-libs/common-dto/

**Purpose**: Shared data transfer objects

- Cross-service communication models
- API request/response DTOs
- Prevents code duplication

#### shared-libs/common-utils/

**Purpose**: Utility classes and helpers

- Date/time utilities
- String validators
- Common converters
- Used by: All services

#### shared-libs/event-models/

**Purpose**: Event-driven architecture models

- Event definitions
- Message payloads
- RabbitMQ integration models
- Used by: Services publishing/consuming events

### Business Services

#### services/automobile-service/

**Purpose**: Vehicle management and tracking

- **Port**: 8082
- **Database**: `as_automobile_service`
- **Tables**: `vehicles`
- **Features**:
  - Vehicle CRUD operations
  - Vehicle tracking
  - PostgreSQL persistence

#### services/notification-service/

**Purpose**: User notifications and alerts

- **Port**: 8081
- **Database**: `as_notification_service`
- **Tables**: `notifications`
- **Features**:
  - Email/SMS notifications
  - Event-driven triggers
  - Firebase Cloud Messaging ready
  - RabbitMQ consumer

#### services/user-auth-service/

**Purpose**: User authentication and authorization

- **Port**: 8083
- **Database**: `as_user_auth_service`
- **Tables**: `users`, `user_sessions`
- **Status**: ⚠️ Scaffold only - needs implementation
- **Planned Features**:
  - User registration
  - JWT token management
  - Session handling
  - Role-based access control

## 🗄️ Database Architecture

### Database-per-Service Pattern

Each service has its own isolated PostgreSQL database:

```
PostgreSQL Server (Port 5432)
├── as_automobile_service (user: auto_user)
├── as_notification_service (user: notification_user)
└── as_user_auth_service (user: auth_user)
```

### Adding a Database for Your Service

1. **Edit** `deployment/postgres/init-db.sql`
2. **Add database creation**:
   ```sql
   CREATE DATABASE as_your_service;
   CREATE USER your_user WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE as_your_service TO your_user;
   ```
3. **Add to** `.env`:
   ```
   YOUR_SERVICE_DB_URL=jdbc:postgresql://db:5432/as_your_service
   YOUR_SERVICE_DB_USER=your_user
   YOUR_SERVICE_DB_PASSWORD=password
   ```
4. **Restart** PostgreSQL container:
   ```powershell
   docker compose -f deployment/docker/docker-compose.yml restart db
   ```

See `POSTGRES_SETUP.md` for detailed database documentation.

## Run

You can run the services locally in Docker (recommended for microservices) or run modules individually with Maven.

Docker (recommended):

1. Ensure Docker Desktop / daemon is running.
2. Copy `.env.example` to `.env` and set `FIREBASE_CREDENTIALS_HOST_PATH` to the host path of your Firebase JSON.
3. From the repo root run with Docker Compose (cross-platform):

Note: the repository's compose files live under `deployment/docker/`. You must point Docker Compose at that file (or run the commands from that folder). Below are the recommended cross-platform commands and alternatives for older Docker installations.

```powershell
# Preferred (Docker Compose v2 - `docker compose`)
# Build images and start containers in detached mode
docker compose -f deployment/docker/docker-compose.yml up --build -d

# View logs (follow)
docker compose -f deployment/docker/docker-compose.yml logs -f

# Stop and remove containers
docker compose -f deployment/docker/docker-compose.yml down
```

If you have the legacy `docker-compose` binary instead of the newer `docker compose` CLI, use the same commands but with a hyphen:

```powershell
docker-compose -f deployment/docker/docker-compose.yml up --build -d
docker-compose -f deployment/docker/docker-compose.yml logs -f
docker-compose -f deployment/docker/docker-compose.yml down
```

If you prefer to run services directly via Maven (dev mode):

```bash
# From the module folder (example: main)
./mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run       # macOS/Linux (if using the unix wrapper)

# Or build and run the jar
./mvnw.cmd clean package     # Windows
./mvnw clean package         # macOS/Linux
java -jar main/target/demo-0.0.1-SNAPSHOT.jar
```

## Database Setup (PostgreSQL)

This project uses PostgreSQL 15 with a **database-per-service** architecture. Each microservice has its own isolated database with dedicated users and permissions. **Flyway** is used for version-controlled database migrations.

### Quick Start

1. **Copy environment file**:

```powershell
Copy-Item .env.example .env
```

2. **Configure database credentials** in `.env`:

```
AUTOMOBILE_DB_URL=jdbc:postgresql://gearup-postgres:5432/as_automobile_service
AUTOMOBILE_DB_USER=auto_user
AUTOMOBILE_DB_PASSWORD=auto_secure_pass_2024
# ... (similar for notification and user-auth services)
```

3. **Deploy with automated setup**:

```powershell
.\scripts\deploy.ps1
```

This will:

- Build all services with Maven
- Start PostgreSQL container with automatic database/user initialization
- Run Flyway migrations to create tables and schema
- Run database connection tests
- Perform health checks on all services

### Database Migration with Flyway

This project uses **Flyway** for version-controlled, automated database migrations. Flyway migrations are located in each service's `src/main/resources/db/migration/` directory.

#### Migration File Structure

```
services/
├── automobile-service/
│   └── src/main/resources/db/migration/
│       ├── V1__initial_schema.sql
│       └── V2__add_vehicle_tracking.sql
├── notification-service/
│   └── src/main/resources/db/migration/
│       └── V1__initial_schema.sql
└── user-auth-service/
    └── src/main/resources/db/migration/
        └── V1__initial_schema.sql
```

#### Running Migrations Manually

```powershell
# Run migrations for all services
make flyway-all              # Unix/macOS
# Or manually via Maven:
.\mvnw.cmd flyway:migrate -pl services/automobile-service
.\mvnw.cmd flyway:migrate -pl services/notification-service
.\mvnw.cmd flyway:migrate -pl services/user-auth-service
.\mvnw.cmd flyway:migrate -pl services/template-service

# Run migrations for specific service
make flyway-automobile       # Unix/macOS
.\mvnw.cmd -pl services/automobile-service flyway:migrate

# Check migration status
.\mvnw.cmd -pl services/automobile-service flyway:info

# Validate migrations
.\mvnw.cmd -pl services/automobile-service flyway:validate
```

#### Creating New Migrations

1. Create a new SQL file in `services/your-service/src/main/resources/db/migration/`
2. Follow naming convention: `V{version}__{description}.sql`
   - Example: `V2__add_vehicle_status_column.sql`
3. Write idempotent SQL (migrations run only once)
4. Test locally before committing

Example migration:

```sql
-- V2__add_vehicle_status_column.sql
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS status_updated_at TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_vehicles_status_updated ON vehicles(status_updated_at);
```

#### Migration Best Practices

- ✅ **Always use versioned migrations** (`V{number}__{description}.sql`)
- ✅ **Write idempotent SQL** (use `IF NOT EXISTS`, `IF EXISTS`)
- ✅ **Test migrations locally** before pushing
- ✅ **Never modify existing migrations** (create new ones instead)
- ✅ **Use descriptive names** for clarity
- ✅ **Keep migrations small** and focused
- ❌ **Never delete old migrations** (breaks Flyway checksums)

#### Flyway Configuration

Each service's `application.properties` contains Flyway configuration:

```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true
spring.flyway.out-of-order=false
```

#### Initial Database Setup Process

1. **PostgreSQL container starts** with `deployment/postgres/init-db.sql`

   - Creates databases: `as_automobile_service`, `as_notification_service`, `as_user_auth_service`, `as_template_service`
   - Creates service users with credentials
   - Grants permissions

2. **Services start** and Flyway runs automatically:

   - Creates `flyway_schema_history` table
   - Executes pending migrations in order
   - Validates checksums

3. **Tables are created** by migration scripts:
   - `V1__initial_schema.sql` creates initial tables
   - Future migrations apply incrementally

For detailed setup information, see `POSTGRES_SETUP.md`.

## 🧪 Testing

### Run All Tests

```powershell
# Unit tests for all modules
.\mvnw.cmd test

# Generate coverage report
.\mvnw.cmd clean verify
# View report: target/site/jacoco/index.html
```

### Test Specific Service

```powershell
# Test single service
.\mvnw.cmd -pl services/notification-service -am test

# Skip tests during build
.\mvnw.cmd clean package -DskipTests
```

### Integration Testing

```powershell
# Verify database connections
.\scripts\test-db-connections.ps1

# Check all service health endpoints
.\scripts\health-check.ps1

# Full deployment verification
.\scripts\verify-deployment.ps1
```

### Manual Testing

```powershell
# Test public endpoint
curl http://localhost:8080/api/public/hello

# Test secured endpoint (requires Firebase token)
curl -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" http://localhost:8080/api/secure/hello

# Test via API Gateway routing
curl http://localhost:8080/api/notification-service/health
curl http://localhost:8080/api/automobile-service/vehicles
```

## 🛠️ Development Workflow

### Local Development (Maven)

```powershell
# Start infrastructure first
docker compose -f deployment/docker/docker-compose.yml up -d db service-discovery config-server

# Run your service locally
cd services/your-service
..\..\mvnw.cmd spring-boot:run

# Or build and run JAR
..\..\mvnw.cmd clean package
java -jar target/your-service-1.0.0.jar
```

### Docker Development

```powershell
# Build and start all services
docker compose -f deployment/docker/docker-compose.yml up --build -d

# View logs for specific service
docker logs -f gearup-notification-service

# Restart single service
docker compose -f deployment/docker/docker-compose.yml restart notification-service

# Stop all services
docker compose -f deployment/docker/docker-compose.yml down
```

### Hot Reload Development

```powershell
# Add to pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

# Run with dev profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📦 Building & Deployment

### Build All Modules

```powershell
# Clean build with tests
.\mvnw.cmd clean package

# Skip tests (faster)
.\mvnw.cmd clean package -DskipTests

# Build specific module
.\mvnw.cmd -pl services/notification-service -am clean package
```

### Docker Build

```powershell
# Build images for all services
docker compose -f deployment/docker/docker-compose.yml build

# Build specific service
docker compose -f deployment/docker/docker-compose.yml build notification-service

# No cache rebuild
docker compose -f deployment/docker/docker-compose.yml build --no-cache
```

### Deploy to Kubernetes

```powershell
# Apply all manifests
kubectl apply -f k8s/namespaces/
kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/

# Verify deployment
kubectl get pods -n auto-service-system
kubectl get services -n auto-service-system

# Check logs
kubectl logs -f deployment/notification-service -n auto-service-system
```

See `deployment/README.md` for detailed deployment guides.

## 🔧 Configuration Management

### Environment Variables (.env)

```bash
# Firebase Authentication
FIREBASE_CREDENTIALS_HOST_PATH=C:\\path\\to\\firebase-service-account.json
FIREBASE_CREDENTIALS_CONTAINER_PATH=/run/secrets/firebase-service-account.json

# PostgreSQL Databases
AUTOMOBILE_DB_URL=jdbc:postgresql://db:5432/as_automobile_service
AUTOMOBILE_DB_USER=auto_user
AUTOMOBILE_DB_PASSWORD=auto_secure_pass_123

NOTIFICATION_DB_URL=jdbc:postgresql://db:5432/as_notification_service
NOTIFICATION_DB_USER=notification_user
NOTIFICATION_DB_PASSWORD=notif_secure_pass_456

USER_AUTH_DB_URL=jdbc:postgresql://db:5432/as_user_auth_service
USER_AUTH_DB_USER=auth_user
USER_AUTH_DB_PASSWORD=auth_secure_pass_789

# Message Queue (RabbitMQ)
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Cache (Redis)
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

### Config Server (config-repo/)

Centralized configuration for all services:

```
config-repo/
├── application.yml          # Global config
├── api-gateway.yml          # Gateway-specific
├── notification-service.yml # Service-specific
└── shared/
    └── logging.yml          # Shared logging config
```

### Profile-Based Configuration

```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
# application-dev.yml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    hikari:
      maximum-pool-size: 5

---
# application-prod.yml
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    hikari:
      maximum-pool-size: 20
```

## 🔒 Security

### ✅ Security Hardening (Updated 2024)

**All hardcoded secrets have been removed from the codebase.**

- ✅ No hardcoded passwords in configuration files
- ✅ No credentials in version control
- ✅ Environment variable-based secret management
- ✅ Fail-fast if environment variables missing

**Documentation**:

- [Security Hardening Guide](docs/SECURITY_HARDENING.md)
- [Security Audit Report](docs/SECURITY_AUDIT_REPORT.md)
- [Quick Reference](docs/SECURITY_QUICK_REFERENCE.md)

**Required Environment Variables**:

```bash
# See .env.example for complete list
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_FIREBASE_CONFIGURATION_FILE  # For automobile & user-auth services
```

**⚠️ Important**: Never commit `.env` or `firebase-service-account.json` to version control!

### Firebase Authentication Flow

1. Client obtains Firebase ID token
2. Client sends request to API Gateway with token in header
3. API Gateway validates token using `FirebaseGatewayFilter`
4. Valid requests are routed to backend services
5. Services can access user info from request headers

### Protected Endpoints

```java
// In your controller
@GetMapping("/secure/data")
public ResponseEntity<?> getSecureData(
    @RequestHeader("X-User-Email") String userEmail,
    @RequestHeader("X-User-UID") String userId
) {
    // User info injected by API Gateway after auth
    return ResponseEntity.ok(yourService.getData(userId));
}
```

### Adding Security to Your Service

```xml
<!-- Add dependency -->
<dependency>
    <groupId>com.gearup</groupId>
    <artifactId>shared-security-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
// Use in your service
@Autowired
private FirebaseAuthService firebaseAuth;

public void verifyToken(String token) {
    FirebaseToken decoded = firebaseAuth.verifyToken(token);
    String uid = decoded.getUid();
}
```

## 📊 Monitoring & Health Checks

### Service Health Endpoints

All services expose Spring Boot Actuator endpoints:

```
GET /actuator/health          # Overall health status
GET /actuator/info            # Service information
GET /actuator/metrics         # Performance metrics
```

### Check Service Health

```powershell
# Via API Gateway
curl http://localhost:8080/api/notification-service/actuator/health

# Direct to service
curl http://localhost:8081/actuator/health

# Automated health check
.\scripts\health-check.ps1
```

### Eureka Dashboard

Monitor all registered services:

- URL: http://localhost:8761
- Shows: Service status, instances, health

### Database Health

```powershell
# Test all database connections
.\scripts\test-db-connections.ps1

# Check PostgreSQL directly
docker exec -it gearup-postgres psql -U auto_user -d as_automobile_service
```

## 🐛 Troubleshooting

### Common Issues

| Problem                    | Solution                                                        |
| -------------------------- | --------------------------------------------------------------- |
| Service won't start        | Check if Eureka/Config Server are running first                 |
| Database connection failed | Verify credentials in `.env`, ensure PostgreSQL is healthy      |
| Service not in Eureka      | Check `@EnableDiscoveryClient` annotation, network connectivity |
| Config not loading         | Ensure config-server is running, check `spring.config.import`   |
| Port already in use        | Change port in `application.properties`                         |
| Firebase auth fails        | Verify `FIREBASE_CREDENTIALS_HOST_PATH` points to valid JSON    |

### Debug Commands

```powershell
# Check Docker containers
docker ps -a

# View service logs
docker logs gearup-notification-service

# Check network connectivity
docker network inspect gearup-backend_default

# Database connection
docker exec -it gearup-postgres psql -U postgres

# Rebuild from scratch
docker compose -f deployment/docker/docker-compose.yml down -v
.\scripts\deploy.ps1
```

### Enable Debug Logging

```properties
# application.properties
logging.level.root=INFO
logging.level.com.gearup=DEBUG
logging.level.org.springframework.cloud=DEBUG
```

## 📁 Project Structure

```
GearUp-backend/
├── api-gateway/                      # API Gateway service
│   ├── src/main/java/                # Gateway filters, config
│   ├── src/main/resources/           # Routes configuration
│   └── pom.xml
├── service-discovery/                # Eureka Server
│   └── src/main/java/                # Discovery server config
├── config-server/                    # Config Server
│   └── src/main/java/                # Config server setup
├── services/                         # Business services
│   ├── automobile-service/           # Vehicle management
│   ├── notification-service/         # Notifications
│   ├── template-service/             # Service template
│   └── user-auth-service/            # Authentication
├── shared-libs/                      # Shared libraries
│   ├── security-lib/                 # Firebase utilities
│   ├── common-dto/                   # Data models
│   ├── common-utils/                 # Utilities
│   └── event-models/                 # Event patterns
├── deployment/                       # Deployment configs
│   ├── docker/
│   │   └── docker-compose.yml        # Local deployment
│   └── postgres/
│       ├── init-db.sql               # Database init
│       └── README.md                 # Database guide
├── k8s/                              # Kubernetes manifests
│   ├── deployments/                  # Deployment configs
│   ├── services/                     # Service definitions
│   ├── configmaps/                   # ConfigMaps
│   └── secrets/                      # Secrets templates
├── config-repo/                      # Centralized configs
│   ├── application.yml               # Global config
│   ├── api-gateway.yml               # Per-service configs
│   └── shared/                       # Shared configs
├── scripts/                          # Automation scripts
│   ├── deploy.ps1                    # Full deployment
│   ├── test-db-connections.ps1       # DB testing
│   ├── health-check.ps1              # Health monitoring
│   └── verify-deployment.ps1         # Verification
├── .env.example                      # Environment template
├── pom.xml                           # Root POM
└── README.md                         # This file
```

## 🔗 Useful Links

- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **API Gateway**: http://localhost:8080
- **Notification Service**: http://localhost:8081/actuator/health
- **Automobile Service**: http://localhost:8082/actuator/health

## 📖 Additional Documentation

- `POSTGRES_SETUP.md` - Database setup and management
- `INFRASTRUCTURE_STATUS.md` - Infrastructure readiness checklist
- `deployment/README.md` - Deployment guides
- `docs/DEV_GUIDE.md` - Developer quickstart
- `docs/CONTAINERS.md` - Container management
- Individual service READMEs in `services/*/README.md`

## 🤝 Contributing

### For Team Members

1. **Clone and setup**:

   ```powershell
   git clone <repository-url>
   cd GearUp-backend
   Copy-Item .env.example .env
   # Edit .env with your credentials
   ```

2. **Start infrastructure**:

   ```powershell
   .\scripts\deploy.ps1
   ```

3. **Create your service** (see "Building New Services" section above)

4. **Test your changes**:

   ```powershell
   .\mvnw.cmd test
   .\scripts\verify-deployment.ps1
   ```

5. **Submit**:
   ```powershell
   git checkout -b feature/your-service
   git add .
   git commit -m "Add: your service implementation"
   git push origin feature/your-service
   ```

### Code Standards

- Java 21 language features
- Spring Boot 3.5.7 conventions
- RESTful API design
- Comprehensive JavaDoc for public methods
- Unit tests with >70% coverage
- Follow existing service structure

## 📞 Support

### Getting Help

1. Check this README and module-specific documentation
2. Review `docs/` directory for guides
3. Check Eureka dashboard for service status
4. Review service logs: `docker logs gearup-<service-name>`
5. Contact team lead for infrastructure questions

### Quick Reference

```powershell
# Full deployment
.\scripts\deploy.ps1

# Health check
.\scripts\health-check.ps1

# Database test
.\scripts\test-db-connections.ps1

# Build all
.\mvnw.cmd clean package

# Run tests
.\mvnw.cmd test
```
