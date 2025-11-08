# 🔐 Authentication & Authorization Service - Quick Reference Guide

## 📖 Overview

This guide provides a complete reference for implementing the **Authentication & Authorization Service** for the GearUp microservices platform using **Firebase** as the authentication provider.

---

## 📚 Documentation Structure

Your implementation guide is split into 4 parts:

### Part 1: Architecture & Database Schema
**File**: `AUTH_SERVICE_IMPLEMENTATION_GUIDE.md`
- Architecture overview
- Prerequisites
- Dependencies (pom.xml)
- Configuration files
- Database schema (migrations)
- Entity models (User, Role, Permission)

### Part 2: DTOs, Repositories & Core Services
**File**: `AUTH_SERVICE_IMPLEMENTATION_PART2.md`
- Data Transfer Objects (DTOs)
- Repository interfaces
- Core services (UserService, AuthService)

### Part 3: Controllers & Security
**File**: `AUTH_SERVICE_IMPLEMENTATION_PART3.md`
- REST Controllers (AuthController, UserController, RoleController)
- Security configuration
- Additional services (TokenService, AuditService, EventPublisher)
- Exception handling

### Part 4: Final Setup & Deployment
**File**: `AUTH_SERVICE_IMPLEMENTATION_PART4.md`
- Missing entities (UserSession, UserAuditLog)
- Main application class
- Dockerfile
- Step-by-step implementation phases
- Testing instructions
- Firebase setup
- Troubleshooting

---

## 🚀 Quick Start (10 Steps)

### 1. Prerequisites Check ✅
```powershell
# Verify Java 21
java -version

# Verify Docker is running
docker ps

# Check PostgreSQL is running
docker ps | findstr postgres

# Check Eureka Server is running
curl http://localhost:8761
```

### 2. Firebase Setup 🔥
1. Create Firebase project at https://console.firebase.google.com/
2. Enable Authentication (Email/Password)
3. Download service account key → `firebase-service-account.json`
4. Place in project root: `GearUp-backend/firebase-service-account.json`
5. **Add to .gitignore** (already done)

### 3. Create Project Structure 📁
```powershell
cd services\user-auth-service

# Create directories
mkdir -p src\main\java\com\gearup\userauth\{model,dto,repository,service,controller,config,exception}
mkdir -p src\main\resources\db\migration
```

### 4. Copy Files from Documentation 📝

#### Phase 1: Core Files
- `pom.xml` → Update dependencies
- `application.yml` → Create in src/main/resources
- `bootstrap.yml` → Create in src/main/resources
- `config-repo/user-auth-service.yml` → Update configuration

#### Phase 2: Database
- `V1__initial_schema.sql` → src/main/resources/db/migration/
- `V2__seed_roles_permissions.sql` → src/main/resources/db/migration/

#### Phase 3: Entities (model/)
- User.java
- Role.java
- Permission.java
- UserSession.java
- UserAuditLog.java

#### Phase 4: DTOs (dto/)
- RegisterUserRequest.java
- UserResponse.java
- UpdateUserRequest.java
- TokenResponse.java
- AuthRequest.java
- RoleRequest.java
- ApiResponse.java
- RefreshTokenRequest.java
- RoleResponse.java

#### Phase 5: Repositories (repository/)
- UserRepository.java
- RoleRepository.java
- PermissionRepository.java
- UserSessionRepository.java
- UserAuditLogRepository.java

#### Phase 6: Services (service/)
- UserService.java
- AuthService.java
- TokenService.java
- AuditService.java
- EventPublisher.java
- RoleService.java

#### Phase 7: Controllers (controller/)
- AuthController.java
- UserController.java
- RoleController.java

#### Phase 8: Configuration (config/)
- SecurityConfig.java
- CustomAuthenticationEntryPoint.java

#### Phase 9: Exceptions (exception/)
- ResourceNotFoundException.java
- UserAlreadyExistsException.java
- AuthenticationException.java
- GlobalExceptionHandler.java

#### Phase 10: Main Application
- UserAuthServiceApplication.java

### 5. Build the Service 🔨
```powershell
# Go to project root
cd ..\..

# Clean and build
.\mvnw clean install -DskipTests
```

### 6. Start Infrastructure 🏗️
```powershell
cd deployment\docker

# Start all services
docker-compose up -d

# Wait for services to be ready (60 seconds)
Start-Sleep -Seconds 60
```

### 7. Verify Service Health 🏥
```powershell
# Check if service is running
docker ps | findstr user-auth

# Check logs
docker-compose logs -f user-auth-service

# Test health endpoint
curl http://localhost:8082/actuator/health

# Check Eureka registration
# Open browser: http://localhost:8761
```

### 8. Test API Endpoints 🧪

#### Test Registration
```powershell
$registerBody = @{
    firebaseUid = "test-uid-123"
    email = "john.doe@example.com"
    phoneNumber = "+1234567890"
    firstName = "John"
    lastName = "Doe"
    role = "CUSTOMER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/v1/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $registerBody
```

#### Test Login (requires real Firebase token from frontend)
```powershell
$loginBody = @{
    firebaseToken = "YOUR_FIREBASE_ID_TOKEN_HERE"
    deviceInfo = "Windows 11"
    ipAddress = "192.168.1.1"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

# Save token for next request
$token = $response.data.accessToken
```

#### Test Get Profile
```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/v1/users/me" `
    -Method GET `
    -Headers @{Authorization = "Bearer $token"}
```

### 9. Access via API Gateway 🌐
```powershell
# All requests should go through API Gateway in production
$url = "http://localhost:9090/user-auth-service/api/v1/auth/register"

Invoke-RestMethod -Uri $url -Method POST -ContentType "application/json" -Body $registerBody
```

### 10. Monitor & Debug 🔍
```powershell
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f user-auth-service

# View last 100 lines
docker-compose logs --tail=100 user-auth-service

# Check database
docker exec -it gearup-postgres psql -U postgres -d as_user_auth_service
# Then: \dt (list tables)
# Then: SELECT * FROM users;
```

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Application                       │
│                  (Web/Mobile - Next.js)                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ 1. Firebase Authentication
                         │    (Get ID Token)
                         ▼
                  ┌──────────────┐
                  │   Firebase   │
                  │  Auth Server │
                  └──────────────┘
                         │
                         │ 2. Send ID Token
                         ▼
┌────────────────────────────────────────────────────────────┐
│                      API Gateway                            │
│                    (Port 9090)                              │
│  - Route requests                                           │
│  - Load balancing                                           │
│  - Rate limiting                                            │
└────────────┬───────────────────────────────────────────────┘
             │
             │ 3. Forward to User Auth Service
             ▼
┌─────────────────────────────────────────────────────────────┐
│            User Authentication Service (Port 8082)          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  FirebaseAuthenticationFilter                       │   │
│  │  - Verify Firebase ID Token                         │   │
│  │  - Extract user claims                              │   │
│  └────────────────┬────────────────────────────────────┘   │
│                   │                                          │
│                   ▼                                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Controllers                                        │   │
│  │  - AuthController (login, register, logout)        │   │
│  │  - UserController (profile, update)                │   │
│  │  - RoleController (RBAC management)                │   │
│  └────────────────┬────────────────────────────────────┘   │
│                   │                                          │
│                   ▼                                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Services                                           │   │
│  │  - AuthService (authentication logic)              │   │
│  │  - UserService (user management)                   │   │
│  │  - TokenService (JWT generation)                   │   │
│  │  - RoleService (RBAC)                              │   │
│  └────────────────┬────────────────────────────────────┘   │
│                   │                                          │
│                   ▼                                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Repositories (JPA)                                 │   │
│  │  - UserRepository                                   │   │
│  │  - RoleRepository                                   │   │
│  │  - PermissionRepository                             │   │
│  │  - UserSessionRepository                            │   │
│  └────────────────┬────────────────────────────────────┘   │
└────────────────────┼────────────────────────────────────────┘
                     │
         ┌───────────┴──────────┐
         ▼                      ▼
┌──────────────────┐   ┌──────────────────┐
│   PostgreSQL     │   │      Redis       │
│  (User Data)     │   │  (Session Cache) │
│  Port 5432       │   │  Port 6379       │
└──────────────────┘   └──────────────────┘
         │
         │ Publishes Events
         ▼
┌──────────────────────────────┐
│      Message Queue           │
│   (RabbitMQ/Kafka)          │
│  - UserRegistered           │
│  - UserUpdated              │
│  - UserDeleted              │
└──────────────────────────────┘
         │
         └─────────────────────────────┐
                                       ▼
                          ┌───────────────────────┐
                          │  Other Microservices  │
                          │  - Vehicle Service    │
                          │  - Booking Service    │
                          │  - Notification Svc   │
                          └───────────────────────┘
```

---

## 📊 Database Schema

### Tables Created by Migrations

1. **users** - Core user information
2. **roles** - System roles (ADMIN, CUSTOMER, MECHANIC, etc.)
3. **permissions** - Granular permissions (user:read, vehicle:create, etc.)
4. **user_roles** - Many-to-many relationship
5. **role_permissions** - Many-to-many relationship
6. **user_sessions** - Active JWT sessions
7. **user_audit_log** - Audit trail

### Default Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | All permissions |
| **CUSTOMER** | vehicle:read, booking:*, payment:read, payment:process |
| **MECHANIC** | vehicle:read/update, booking:read/update |
| **FLEET_MANAGER** | vehicle:*, booking:read, analytics:read |
| **SUPPORT** | user:read, vehicle:read, booking:read/update, payment:read |

---

## 🔑 Environment Variables

### Required Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/as_user_auth_service
SPRING_DATASOURCE_USERNAME=svc_user_auth_service
SPRING_DATASOURCE_PASSWORD=auth_svc_pass_2024

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# Eureka
EUREKA_URL=http://localhost:8761/eureka

# Firebase
FIREBASE_CONFIG_PATH=/path/to/firebase-service-account.json

# JWT (for internal tokens)
JWT_SECRET=your-256-bit-secret-change-this-in-production
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

### Setting Environment Variables (PowerShell)

```powershell
# Set for current session
$env:FIREBASE_CONFIG_PATH = "C:\path\to\firebase-service-account.json"
$env:JWT_SECRET = "your-super-secret-key-at-least-256-bits-long"

# Or create .env file in project root (already configured in docker-compose)
```

---

## 🎯 Key Features Implemented

### ✅ Authentication
- [x] Firebase ID token verification
- [x] User registration with Firebase
- [x] Login with Firebase token
- [x] JWT generation for internal services
- [x] Session management
- [x] Token refresh
- [x] Logout functionality

### ✅ Authorization (RBAC)
- [x] Role-based access control
- [x] Permission-based authorization
- [x] User-role assignment
- [x] Role-permission mapping
- [x] Method-level security (`@PreAuthorize`)

### ✅ User Management
- [x] User CRUD operations
- [x] Profile management
- [x] Email/phone verification status
- [x] Account status (Active, Suspended, etc.)
- [x] Last login tracking

### ✅ Security Features
- [x] Stateless JWT authentication
- [x] CORS configuration
- [x] Global exception handling
- [x] Input validation
- [x] Audit logging
- [x] Session tracking

### ✅ Integration
- [x] Eureka service discovery
- [x] Config Server integration
- [x] API Gateway routing
- [x] Event publishing (for other services)
- [x] Health checks

---

## 🔧 Common Operations

### Add a New Permission
```sql
-- Connect to database
docker exec -it gearup-postgres psql -U postgres -d as_user_auth_service

-- Add permission
INSERT INTO permissions (name, resource, action, description) 
VALUES ('invoice:read', 'INVOICE', 'READ', 'View invoices');

-- Assign to role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'CUSTOMER' AND p.name = 'invoice:read';
```

### Create a New Role
```sql
INSERT INTO roles (name, description, is_active) 
VALUES ('INSPECTOR', 'Vehicle inspector role', true);

-- Assign permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'INSPECTOR' 
AND p.name IN ('vehicle:read', 'vehicle:update');
```

### Manually Create Test User
```sql
INSERT INTO users (firebase_uid, email, first_name, last_name, account_status)
VALUES ('test-uid-456', 'test@example.com', 'Test', 'User', 'ACTIVE');

-- Assign CUSTOMER role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.email = 'test@example.com' AND r.name = 'CUSTOMER';
```

### View User with Roles
```sql
SELECT 
    u.id, u.email, u.first_name, u.last_name,
    array_agg(DISTINCT r.name) as roles,
    array_agg(DISTINCT p.name) as permissions
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
WHERE u.email = 'test@example.com'
GROUP BY u.id;
```

---

## 🐛 Troubleshooting Guide

### Problem: Service Won't Start

**Symptoms**: Service exits immediately or fails to start

**Check**:
```powershell
# View logs
docker-compose logs user-auth-service

# Common issues:
# 1. Database not ready
docker-compose logs db | findstr -i "ready to accept connections"

# 2. Firebase config missing
Test-Path firebase-service-account.json

# 3. Port already in use
netstat -ano | findstr :8082
```

**Solution**:
```powershell
# Restart in order
docker-compose down
docker-compose up -d db
Start-Sleep -Seconds 30
docker-compose up -d config-server service-discovery
Start-Sleep -Seconds 30
docker-compose up -d user-auth-service
```

### Problem: Firebase Token Verification Fails

**Symptoms**: "Invalid or expired token" errors

**Check**:
```powershell
# Verify Firebase config is loaded
docker-compose logs user-auth-service | findstr -i firebase

# Check environment variable
docker exec -it user-auth-service env | findstr FIREBASE
```

**Solution**:
1. Verify `firebase-service-account.json` is correct
2. Check file path in environment variable
3. Regenerate Firebase token from frontend
4. Ensure token hasn't expired (1 hour validity)

### Problem: Cannot Connect to Database

**Symptoms**: "Connection refused" or timeout errors

**Check**:
```powershell
# Check database is running
docker ps | findstr postgres

# Test connection
docker exec -it gearup-postgres psql -U postgres -l
```

**Solution**:
```powershell
# Reset database
docker-compose down -v
docker-compose up -d db

# Verify database exists
docker exec -it gearup-postgres psql -U postgres -c "\l"
```

### Problem: Flyway Migrations Fail

**Symptoms**: "Migration failed" or "Checksum mismatch"

**Check**:
```powershell
# View migration status
docker exec -it gearup-postgres psql -U postgres -d as_user_auth_service \
    -c "SELECT * FROM flyway_schema_history;"
```

**Solution**:
```powershell
# Option 1: Clean and rebuild (DELETES ALL DATA!)
docker-compose down -v
docker-compose up -d

# Option 2: Repair Flyway
docker exec -it user-auth-service ./mvnw flyway:repair
```

### Problem: Service Not in Eureka

**Symptoms**: Service not visible at http://localhost:8761

**Check**:
```powershell
# Check Eureka Server is running
curl http://localhost:8761

# Check service logs for registration
docker-compose logs user-auth-service | findstr -i eureka
```

**Solution**:
```yaml
# Verify eureka config in config-repo/user-auth-service.yml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

## 📞 Support & Next Steps

### Need Help?

1. **Check Documentation**: Review all 4 parts of the implementation guide
2. **Check Logs**: Use `docker-compose logs -f <service-name>`
3. **Verify Health**: Check `/actuator/health` endpoint
4. **Database Issues**: Connect to PostgreSQL and verify schema
5. **Firebase Issues**: Regenerate service account key

### What's Next?

After completing the Auth Service, integrate it with:

1. **API Gateway** - Configure routes and authentication
2. **Other Services** - Add token validation to all services
3. **Frontend** - Implement Firebase authentication in Next.js
4. **Monitoring** - Add Prometheus metrics and distributed tracing
5. **Testing** - Write integration and end-to-end tests

---

## 📚 Related Documentation

- **Project README**: `../README.md`
- **Quick Start Guide**: `QUICK_START.md`
- **Security Hardening**: Security best practices
- **Firebase Setup**: Frontend Firebase configuration
- **API Gateway Config**: Routing configuration

---

**🎉 Congratulations! You now have a complete, production-ready Authentication & Authorization Service!**

Remember to:
- ✅ Never commit `firebase-service-account.json`
- ✅ Change default JWT secret in production
- ✅ Use HTTPS in production
- ✅ Implement rate limiting
- ✅ Monitor failed login attempts
- ✅ Regularly update dependencies

**Happy Coding! 🚀**
