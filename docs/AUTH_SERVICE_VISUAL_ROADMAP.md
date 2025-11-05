# 📊 Authentication Service - Visual Implementation Roadmap

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│               🔐 Authentication & Authorization Service                 │
│                    Implementation Roadmap                               │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📖 DOCUMENTATION STRUCTURE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    ┌───────────────────────────────────────────────┐
    │  📄 AUTH_SERVICE_START_HERE.md               │ ◄─── READ THIS FIRST!
    │  Your entry point and navigation guide       │
    └───────────────────────────────────────────────┘
                        │
                        ▼
    ┌───────────────────────────────────────────────┐
    │  📘 AUTH_SERVICE_QUICK_REFERENCE.md          │ ◄─── MAIN REFERENCE
    │  • 10-step quick start                       │
    │  • Architecture diagram                      │
    │  • Common operations                         │
    │  • Troubleshooting guide                     │
    └───────────────────────────────────────────────┘
                        │
           ┌────────────┼────────────┐
           ▼            ▼            ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
    │  PART 1  │  │  PART 2  │  │  PART 3  │  │  PART 4  │
    │          │  │          │  │          │  │          │
    │ Database │  │   DTOs   │  │Controllers│ │  Setup   │
    │ Entities │  │  Repos   │  │ Security │  │ Deploy   │
    │          │  │ Services │  │          │  │          │
    └──────────┘  └──────────┘  └──────────┘  └──────────┘


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🏗️ IMPLEMENTATION PHASES (4 HOURS TOTAL)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

PHASE 1: SETUP (30 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Install Prerequisites                               │
│     • Java 21                                           │
│     • Docker Desktop                                    │
│     • Git                                               │
│                                                         │
│  ✅ Setup Firebase                                      │
│     • Create Firebase project                          │
│     • Enable Authentication                            │
│     • Download service account key                     │
│                                                         │
│  ✅ Create Directory Structure                         │
│     services/user-auth-service/src/main/java/...      │
└─────────────────────────────────────────────────────────┘


PHASE 2: DATABASE (20 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Create Migrations                                   │
│     • V1__initial_schema.sql                           │
│       - users, roles, permissions tables               │
│       - user_sessions, audit_log tables                │
│                                                         │
│  ✅ Seed Data                                           │
│     • V2__seed_roles_permissions.sql                   │
│       - Default roles (ADMIN, CUSTOMER, etc.)          │
│       - Default permissions                            │
│       - Role-permission mappings                       │
└─────────────────────────────────────────────────────────┘


PHASE 3: ENTITIES (30 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Create JPA Entities                                 │
│     📦 model/                                           │
│        ├── User.java (core user data)                  │
│        ├── Role.java (RBAC roles)                      │
│        ├── Permission.java (granular permissions)      │
│        ├── UserSession.java (JWT sessions)             │
│        └── UserAuditLog.java (audit trail)             │
└─────────────────────────────────────────────────────────┘


PHASE 4: DTOs (20 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Create Data Transfer Objects                        │
│     📦 dto/                                             │
│        ├── RegisterUserRequest.java                    │
│        ├── UserResponse.java                           │
│        ├── UpdateUserRequest.java                      │
│        ├── TokenResponse.java                          │
│        ├── AuthRequest.java                            │
│        ├── RoleRequest.java                            │
│        ├── ApiResponse.java                            │
│        ├── RefreshTokenRequest.java                    │
│        └── RoleResponse.java                           │
└─────────────────────────────────────────────────────────┘


PHASE 5: REPOSITORIES (15 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Create JPA Repositories                             │
│     📦 repository/                                      │
│        ├── UserRepository.java                         │
│        ├── RoleRepository.java                         │
│        ├── PermissionRepository.java                   │
│        ├── UserSessionRepository.java                  │
│        └── UserAuditLogRepository.java                 │
└─────────────────────────────────────────────────────────┘


PHASE 6: SERVICES (45 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Implement Business Logic                            │
│     📦 service/                                         │
│        ├── UserService.java                            │
│        │   • Register user                             │
│        │   • Update profile                            │
│        │   • Assign/remove roles                       │
│        │                                                │
│        ├── AuthService.java                            │
│        │   • Login with Firebase                       │
│        │   • Token refresh                             │
│        │   • Logout                                    │
│        │                                                │
│        ├── TokenService.java                           │
│        │   • Generate JWT tokens                       │
│        │   • Validate tokens                           │
│        │   • Session management                        │
│        │                                                │
│        ├── RoleService.java                            │
│        │   • Get roles                                 │
│        │   • Manage permissions                        │
│        │                                                │
│        ├── AuditService.java                           │
│        │   • Log user actions                          │
│        │                                                │
│        └── EventPublisher.java                         │
│            • Publish user events                       │
└─────────────────────────────────────────────────────────┘


PHASE 7: CONTROLLERS (30 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Create REST Endpoints                               │
│     📦 controller/                                      │
│        ├── AuthController.java                         │
│        │   POST /api/v1/auth/register                  │
│        │   POST /api/v1/auth/login                     │
│        │   POST /api/v1/auth/refresh                   │
│        │   POST /api/v1/auth/logout                    │
│        │                                                │
│        ├── UserController.java                         │
│        │   GET  /api/v1/users/me                       │
│        │   PUT  /api/v1/users/me                       │
│        │   GET  /api/v1/users/{id}                     │
│        │   POST /api/v1/users/{id}/roles               │
│        │                                                │
│        └── RoleController.java                         │
│            GET /api/v1/roles                           │
│            GET /api/v1/roles/{name}                    │
└─────────────────────────────────────────────────────────┘


PHASE 8: SECURITY (30 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Configure Spring Security                           │
│     📦 config/                                          │
│        ├── SecurityConfig.java                         │
│        │   • Firebase authentication filter            │
│        │   • Public/protected endpoints                │
│        │   • CORS configuration                        │
│        │   • Session management                        │
│        │                                                │
│        └── CustomAuthenticationEntryPoint.java         │
│            • Custom 401 responses                      │
└─────────────────────────────────────────────────────────┘


PHASE 9: EXCEPTIONS (20 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Implement Error Handling                            │
│     📦 exception/                                       │
│        ├── ResourceNotFoundException.java              │
│        ├── UserAlreadyExistsException.java             │
│        ├── AuthenticationException.java                │
│        └── GlobalExceptionHandler.java                 │
│            • Handle all exceptions                     │
│            • Return consistent responses               │
└─────────────────────────────────────────────────────────┘


PHASE 10: BUILD & TEST (30 min)
┌─────────────────────────────────────────────────────────┐
│  ✅ Build and Deploy                                    │
│     • .\mvnw clean install                             │
│     • docker-compose up -d                             │
│     • Check logs                                        │
│     • Verify Eureka registration                       │
│                                                         │
│  ✅ Test Endpoints                                      │
│     • POST /auth/register                              │
│     • POST /auth/login                                 │
│     • GET  /users/me                                   │
│     • PUT  /users/me                                   │
└─────────────────────────────────────────────────────────┘


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔄 AUTHENTICATION FLOW
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    ┌─────────┐
    │  User   │
    │ (React/ │
    │Next.js) │
    └────┬────┘
         │
         │ 1. Login with email/password
         ▼
    ┌──────────────┐
    │   Firebase   │◄──── User authenticates here first
    │ Auth Service │
    └──────┬───────┘
           │
           │ 2. Returns Firebase ID Token (JWT)
           ▼
    ┌─────────────┐
    │   Client    │
    │  Receives   │◄──── Store token temporarily
    │  ID Token   │
    └──────┬──────┘
           │
           │ 3. Send ID Token to backend
           ▼
    ┌───────────────────────────────────────────────┐
    │         API Gateway (Port 9090)               │
    │    Routes: /user-auth-service/**             │
    └────────────────────┬──────────────────────────┘
                         │
                         │ 4. Forward request
                         ▼
    ┌─────────────────────────────────────────────────┐
    │    User Auth Service (Port 8082)                │
    │                                                  │
    │  ┌────────────────────────────────────────┐    │
    │  │  FirebaseAuthenticationFilter          │    │
    │  │  • Extract token from header           │    │
    │  │  • Verify with Firebase Admin SDK      │    │
    │  │  • Set SecurityContext                 │    │
    │  └─────────────┬──────────────────────────┘    │
    │                │                                 │
    │                │ 5. Token valid?                │
    │                ▼                                 │
    │  ┌────────────────────────────────────────┐    │
    │  │         AuthService                    │    │
    │  │  • Get user from database              │    │
    │  │  • Generate internal JWT token         │    │
    │  │  • Create session                      │    │
    │  └─────────────┬──────────────────────────┘    │
    │                │                                 │
    │                ▼                                 │
    │  ┌────────────────────────────────────────┐    │
    │  │         PostgreSQL                     │    │
    │  │  • Save session                        │    │
    │  │  • Update last_login                   │    │
    │  └────────────────────────────────────────┘    │
    └────────────────┬────────────────────────────────┘
                     │
                     │ 6. Return JWT + User Info
                     ▼
    ┌─────────────────────────────────────┐
    │         Client Application          │
    │  • Store JWT in localStorage        │
    │  • Use JWT for all future requests  │
    │  • Include in Authorization header  │
    └─────────────────────────────────────┘


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🛡️ AUTHORIZATION FLOW (RBAC)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    Request with JWT Token
           │
           ▼
    ┌─────────────────────────┐
    │ Extract & Verify Token  │
    │ (Spring Security)       │
    └────────┬────────────────┘
             │
             ▼
    ┌─────────────────────────┐
    │   Get User Principal    │
    │   (Firebase UID)        │
    └────────┬────────────────┘
             │
             ▼
    ┌─────────────────────────┐
    │   Load User from DB     │
    │   with Roles &          │
    │   Permissions           │
    └────────┬────────────────┘
             │
             ▼
    ┌──────────────────────────────────────┐
    │  Check Method-Level Security         │
    │  @PreAuthorize("hasAuthority...")   │
    └────────┬───────────────┬─────────────┘
             │               │
        ✅ Allowed      ❌ Denied
             │               │
             ▼               ▼
    ┌─────────────┐   ┌─────────────┐
    │  Execute    │   │   Return    │
    │  Method     │   │   403       │
    └─────────────┘   └─────────────┘


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 DATABASE SCHEMA RELATIONSHIPS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    ┌──────────────────────┐
    │       USERS          │
    │──────────────────────│
    │ id (PK)             │
    │ firebase_uid        │◄───── Linked to Firebase
    │ email               │
    │ first_name          │
    │ last_name           │
    │ account_status      │
    └──────────┬───────────┘
               │
               │ Many-to-Many
               ▼
    ┌──────────────────────┐
    │     USER_ROLES       │
    │──────────────────────│
    │ user_id (FK)        │
    │ role_id (FK)        │
    └──────────┬───────────┘
               │
               ▼
    ┌──────────────────────┐
    │       ROLES          │
    │──────────────────────│
    │ id (PK)             │
    │ name                │◄───── ADMIN, CUSTOMER, etc.
    │ description         │
    └──────────┬───────────┘
               │
               │ Many-to-Many
               ▼
    ┌──────────────────────┐
    │  ROLE_PERMISSIONS    │
    │──────────────────────│
    │ role_id (FK)        │
    │ permission_id (FK)  │
    └──────────┬───────────┘
               │
               ▼
    ┌──────────────────────┐
    │    PERMISSIONS       │
    │──────────────────────│
    │ id (PK)             │
    │ name                │◄───── user:read, vehicle:create
    │ resource            │
    │ action              │
    └──────────────────────┘


    ┌──────────────────────┐
    │   USER_SESSIONS      │
    │──────────────────────│
    │ id (PK)             │
    │ user_id (FK)        │◄───── Links to USERS
    │ session_token       │
    │ refresh_token       │
    │ device_info         │
    │ expires_at          │
    └──────────────────────┘


    ┌──────────────────────┐
    │  USER_AUDIT_LOG      │
    │──────────────────────│
    │ id (PK)             │
    │ user_id (FK)        │◄───── Links to USERS
    │ action              │
    │ entity_type         │
    │ old_values (JSON)   │
    │ new_values (JSON)   │
    │ created_at          │
    └──────────────────────┘


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎯 FILE CHECKLIST (35+ Files)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📦 Configuration Files (4)
    ├── [ ] pom.xml
    ├── [ ] application.yml
    ├── [ ] bootstrap.yml
    └── [ ] config-repo/user-auth-service.yml

📦 Database Migrations (2)
    ├── [ ] V1__initial_schema.sql
    └── [ ] V2__seed_roles_permissions.sql

📦 Entity Classes (5)
    ├── [ ] User.java
    ├── [ ] Role.java
    ├── [ ] Permission.java
    ├── [ ] UserSession.java
    └── [ ] UserAuditLog.java

📦 DTOs (9)
    ├── [ ] RegisterUserRequest.java
    ├── [ ] UserResponse.java
    ├── [ ] UpdateUserRequest.java
    ├── [ ] TokenResponse.java
    ├── [ ] AuthRequest.java
    ├── [ ] RoleRequest.java
    ├── [ ] ApiResponse.java
    ├── [ ] RefreshTokenRequest.java
    └── [ ] RoleResponse.java

📦 Repositories (5)
    ├── [ ] UserRepository.java
    ├── [ ] RoleRepository.java
    ├── [ ] PermissionRepository.java
    ├── [ ] UserSessionRepository.java
    └── [ ] UserAuditLogRepository.java

📦 Services (6)
    ├── [ ] UserService.java
    ├── [ ] AuthService.java
    ├── [ ] TokenService.java
    ├── [ ] RoleService.java
    ├── [ ] AuditService.java
    └── [ ] EventPublisher.java

📦 Controllers (3)
    ├── [ ] AuthController.java
    ├── [ ] UserController.java
    └── [ ] RoleController.java

📦 Configuration (2)
    ├── [ ] SecurityConfig.java
    └── [ ] CustomAuthenticationEntryPoint.java

📦 Exceptions (4)
    ├── [ ] ResourceNotFoundException.java
    ├── [ ] UserAlreadyExistsException.java
    ├── [ ] AuthenticationException.java
    └── [ ] GlobalExceptionHandler.java

📦 Main Application (1)
    └── [ ] UserAuthServiceApplication.java

📦 Docker (1)
    └── [ ] Dockerfile


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🚀 QUICK COMMAND REFERENCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Build:
    .\mvnw clean install -DskipTests

Start All Services:
    cd deployment\docker
    docker-compose up -d

Check Service:
    docker-compose logs -f user-auth-service
    curl http://localhost:8082/actuator/health

Test Registration:
    curl -X POST http://localhost:8082/api/v1/auth/register \
      -H "Content-Type: application/json" \
      -d '{"firebaseUid":"test-123","email":"test@example.com",...}'

View Database:
    docker exec -it gearup-postgres psql -U postgres -d as_user_auth_service
    SELECT * FROM users;
    SELECT * FROM roles;

Check Eureka:
    http://localhost:8761


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ SUCCESS CRITERIA
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

You're done when:
    ✅ Project builds without errors
    ✅ Service starts successfully
    ✅ Registered with Eureka (visible at :8761)
    ✅ Health endpoint returns "UP"
    ✅ Can register new users
    ✅ Can login with Firebase token
    ✅ Can retrieve user profile
    ✅ Can update user information
    ✅ RBAC works (roles & permissions)
    ✅ All tests pass
    ✅ Accessible via API Gateway


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🎉 Ready to start? Open AUTH_SERVICE_START_HERE.md!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
