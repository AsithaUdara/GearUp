# Authentication & Authorization Service - Part 4: Final Files & Implementation Steps

## 📦 Missing Entity Classes

### UserSession.java
```java
package com.gearup.userauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;
    
    @Column(name = "refresh_token", unique = true, length = 500)
    private String refreshToken;
    
    @Column(name = "device_info", columnDefinition = "TEXT")
    private String deviceInfo;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastAccessedAt = LocalDateTime.now();
    }
}
```

### UserAuditLog.java
```java
package com.gearup.userauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String action;
    
    @Column(name = "entity_type", length = 100)
    private String entityType;
    
    @Column(name = "entity_id", length = 100)
    private String entityId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private Map<String, Object> oldValues;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private Map<String, Object> newValues;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

## 🚀 Main Application Class

### UserAuthServiceApplication.java
```java
package com.gearup.userauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {
    "com.gearup.userauth",
    "com.gearup.security"  // Include shared security lib
})
@EnableJpaRepositories(basePackages = "com.gearup.userauth.repository")
public class UserAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
```

---

## 📝 Application Properties

### application.yml
Create `services/user-auth-service/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: user-auth-service
  
  config:
    import: "optional:configserver:http://localhost:8888"
  
  cloud:
    config:
      enabled: true
      fail-fast: false
      uri: http://localhost:8888

server:
  port: 8082

# Logging
logging:
  level:
    root: INFO
    com.gearup: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### bootstrap.yml
Create `services/user-auth-service/src/main/resources/bootstrap.yml`:

```yaml
spring:
  application:
    name: user-auth-service
  cloud:
    config:
      uri: ${CONFIG_SERVER_URI:http://localhost:8888}
      fail-fast: false
```

---

## 🐳 Dockerfile

Create `services/user-auth-service/Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file
COPY target/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]
```

---

## 📋 Step-by-Step Implementation Guide

### Phase 1: Project Setup (30 minutes)

#### Step 1.1: Update pom.xml
```powershell
# Navigate to user-auth-service
cd services\user-auth-service

# Copy the updated pom.xml content from Part 1
# Update the file: services/user-auth-service/pom.xml
```

#### Step 1.2: Update Configuration
```powershell
# Update config-repo/user-auth-service.yml with the content from Part 1
```

#### Step 1.3: Create Directory Structure
```powershell
# Create all necessary directories
mkdir -p src\main\java\com\gearup\userauth\model
mkdir -p src\main\java\com\gearup\userauth\dto
mkdir -p src\main\java\com\gearup\userauth\repository
mkdir -p src\main\java\com\gearup\userauth\service
mkdir -p src\main\java\com\gearup\userauth\controller
mkdir -p src\main\java\com\gearup\userauth\config
mkdir -p src\main\java\com\gearup\userauth\exception
mkdir -p src\main\resources\db\migration
```

### Phase 2: Database Setup (20 minutes)

#### Step 2.1: Create Migration Files
```powershell
# Copy the SQL migration files from Part 1:
# - V1__initial_schema.sql
# - V2__seed_roles_permissions.sql
# Place them in: src/main/resources/db/migration/
```

#### Step 2.2: Update docker-compose.yml
Make sure your database service includes the user-auth database:
```yaml
# Already configured in your existing docker-compose.yml
# Verify it includes: as_user_auth_service
```

### Phase 3: Implement Entities (30 minutes)

Create all entity classes in `src/main/java/com/gearup/userauth/model/`:
- ✅ User.java (from Part 1)
- ✅ Role.java (from Part 1)
- ✅ Permission.java (from Part 1)
- ✅ UserSession.java (from this part)
- ✅ UserAuditLog.java (from this part)

### Phase 4: Implement DTOs (20 minutes)

Create all DTO classes in `src/main/java/com/gearup/userauth/dto/`:
- ✅ RegisterUserRequest.java
- ✅ UserResponse.java
- ✅ UpdateUserRequest.java
- ✅ TokenResponse.java
- ✅ AuthRequest.java
- ✅ RoleRequest.java
- ✅ ApiResponse.java
- ✅ RefreshTokenRequest.java
- ✅ RoleResponse.java

### Phase 5: Implement Repositories (15 minutes)

Create all repository interfaces in `src/main/java/com/gearup/userauth/repository/`:
- ✅ UserRepository.java
- ✅ RoleRepository.java
- ✅ PermissionRepository.java
- ✅ UserSessionRepository.java
- ✅ UserAuditLogRepository.java

### Phase 6: Implement Services (45 minutes)

Create all service classes in `src/main/java/com/gearup/userauth/service/`:
- ✅ UserService.java
- ✅ AuthService.java
- ✅ TokenService.java
- ✅ AuditService.java
- ✅ EventPublisher.java
- ✅ RoleService.java

### Phase 7: Implement Controllers (30 minutes)

Create all controller classes in `src/main/java/com/gearup/userauth/controller/`:
- ✅ AuthController.java
- ✅ UserController.java
- ✅ RoleController.java

### Phase 8: Configure Security (30 minutes)

Create configuration classes in `src/main/java/com/gearup/userauth/config/`:
- ✅ SecurityConfig.java
- ✅ CustomAuthenticationEntryPoint.java

### Phase 9: Exception Handling (20 minutes)

Create exception classes in `src/main/java/com/gearup/userauth/exception/`:
- ✅ ResourceNotFoundException.java
- ✅ UserAlreadyExistsException.java
- ✅ AuthenticationException.java
- ✅ GlobalExceptionHandler.java

### Phase 10: Main Application (10 minutes)

Create main application class:
- ✅ UserAuthServiceApplication.java

### Phase 11: Build & Test (30 minutes)

```powershell
# Step 11.1: Build the project
cd ..\..  # Back to root
.\mvnw clean install -DskipTests

# Step 11.2: Start services
cd deployment\docker
docker-compose up -d

# Step 11.3: Check logs
docker-compose logs -f user-auth-service

# Step 11.4: Verify service is registered
# Open browser: http://localhost:8761
# You should see user-auth-service listed

# Step 11.5: Test health endpoint
# Open browser: http://localhost:8082/actuator/health
```

---

## 🧪 Testing the Service

### Test 1: Register a New User

```bash
# Using curl (PowerShell)
$body = @{
    firebaseUid = "test-firebase-uid-123"
    email = "test@example.com"
    phoneNumber = "+1234567890"
    firstName = "John"
    lastName = "Doe"
    role = "CUSTOMER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:9090/user-auth-service/api/v1/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

### Test 2: Login with Firebase Token

```bash
# First, get Firebase token from your frontend
# Then use it to login:
$body = @{
    firebaseToken = "YOUR_FIREBASE_ID_TOKEN"
    deviceInfo = "Windows 11"
    ipAddress = "192.168.1.1"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:9090/user-auth-service/api/v1/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

### Test 3: Get Current User Profile

```bash
$token = "YOUR_JWT_TOKEN_FROM_LOGIN"

Invoke-RestMethod -Uri "http://localhost:9090/user-auth-service/api/v1/users/me" `
    -Method GET `
    -Headers @{Authorization = "Bearer $token"}
```

---

## 🔥 Firebase Setup Instructions

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add Project"
3. Enter project name: "GearUp"
4. Enable Google Analytics (optional)
5. Click "Create Project"

### Step 2: Enable Authentication

1. In Firebase Console, go to "Authentication"
2. Click "Get Started"
3. Enable sign-in methods:
   - ✅ Email/Password
   - ✅ Google (optional)
   - ✅ Phone (optional)

### Step 3: Get Service Account Key

1. Go to Project Settings (gear icon) > Service Accounts
2. Click "Generate New Private Key"
3. Save the JSON file as `firebase-service-account.json`
4. **IMPORTANT**: Never commit this file to Git!

### Step 4: Configure Backend

```powershell
# Place firebase-service-account.json in:
# Option 1: Project root (recommended)
cp firebase-service-account.json c:\Users\ASUS\Desktop\Final EAD\GearUp-backend\

# Option 2: Set environment variable
$env:FIREBASE_CONFIG_PATH = "C:\path\to\firebase-service-account.json"
```

### Step 5: Frontend Setup

In your Next.js frontend, create `lib/firebase.ts`:

```typescript
import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_PROJECT_ID.firebaseapp.com",
  projectId: "YOUR_PROJECT_ID",
  storageBucket: "YOUR_PROJECT_ID.appspot.com",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
```

---

## 📊 API Endpoints Summary

### Public Endpoints (No Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login with Firebase token |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| GET | `/api/v1/auth/verify-token` | Verify token validity |

### Protected Endpoints (Auth Required)

| Method | Endpoint | Description | Required Permission |
|--------|----------|-------------|---------------------|
| GET | `/api/v1/users/me` | Get current user profile | - |
| PUT | `/api/v1/users/me` | Update current user | - |
| GET | `/api/v1/users/{userId}` | Get user by ID | `user:read` |
| POST | `/api/v1/users/{userId}/roles` | Assign role to user | `user:update` |
| DELETE | `/api/v1/users/{userId}/roles/{roleName}` | Remove role | `user:update` |
| GET | `/api/v1/roles` | Get all roles | `user:read` |
| GET | `/api/v1/roles/{roleName}` | Get role details | `user:read` |

---

## ✅ Checklist for Completion

### Before Starting
- [ ] PostgreSQL is running
- [ ] Redis is running
- [ ] Eureka Server is running
- [ ] Config Server is running
- [ ] Firebase project created
- [ ] Firebase service account key downloaded

### Implementation
- [ ] All dependencies added to pom.xml
- [ ] Database migrations created
- [ ] All entities implemented
- [ ] All DTOs implemented
- [ ] All repositories implemented
- [ ] All services implemented
- [ ] All controllers implemented
- [ ] Security configuration done
- [ ] Exception handling configured
- [ ] Main application class created

### Testing
- [ ] Project builds successfully
- [ ] Service starts without errors
- [ ] Service registers with Eureka
- [ ] Health endpoint responds
- [ ] Can register a new user
- [ ] Can login with Firebase token
- [ ] Can get user profile
- [ ] Can update user profile
- [ ] Roles and permissions work

### Deployment
- [ ] Dockerfile created
- [ ] docker-compose.yml updated
- [ ] Environment variables configured
- [ ] Service accessible via API Gateway

---

## 🚨 Common Issues & Solutions

### Issue 1: Firebase Initialization Failed
**Error**: "Failed to initialize Firebase"

**Solution**:
```powershell
# Check if firebase-service-account.json exists
Test-Path firebase-service-account.json

# Set correct path in environment variable
$env:FIREBASE_CONFIG_PATH = "$(pwd)\firebase-service-account.json"
```

### Issue 2: Database Connection Failed
**Error**: "Connection refused to PostgreSQL"

**Solution**:
```powershell
# Check if PostgreSQL is running
docker ps | findstr postgres

# Check database credentials in config-repo/user-auth-service.yml
```

### Issue 3: Flyway Migration Failed
**Error**: "Flyway migration failed"

**Solution**:
```powershell
# Reset database
docker-compose down -v
docker-compose up -d db

# Wait 30 seconds for database to be ready
Start-Sleep -Seconds 30

# Start service again
docker-compose up -d user-auth-service
```

### Issue 4: Service Not Registering with Eureka
**Error**: Service not visible in Eureka dashboard

**Solution**:
```powershell
# Check Eureka is running
curl http://localhost:8761

# Check service logs
docker-compose logs user-auth-service | findstr -i eureka

# Verify eureka.client settings in config
```

---

## 🎯 Next Steps After Implementation

### 1. Integration with Other Services
- Update API Gateway routes
- Configure service-to-service authentication
- Add circuit breakers with Resilience4j

### 2. Add More Features
- Password reset flow
- Email verification
- Two-factor authentication (2FA)
- Social login (Google, Facebook)
- User profile images with cloud storage

### 3. Monitoring & Logging
- Add Prometheus metrics
- Configure distributed tracing
- Set up log aggregation

### 4. Security Hardening
- Implement rate limiting
- Add IP whitelisting
- Enable HTTPS
- Implement account lockout after failed attempts

---

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

---

**You're all set! Follow the phases in order, and you'll have a fully functional Authentication & Authorization Service! 🚀**

If you encounter any issues during implementation, refer to the troubleshooting section or check the service logs.
