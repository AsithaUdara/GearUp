# User Auth Service - Complete Setup Guide

## ✅ **Current Status**

### **user-auth-service** - SUCCESSFULLY CONFIGURED ✅

- **Status**: Service starts and runs successfully
- **Port**: 8082
- **Database**: PostgreSQL 18 (localhost:5434)
- **Database Name**: `as_user_auth_service`
- **Startup Time**: ~6-7 seconds
- **Last Test**: November 6, 2025 - Service started successfully

---

## 🔧 **Working Configuration**

### 1. Database Configuration
```yaml
Database: PostgreSQL 18.0
Host: localhost
Port: 5434
Database: as_user_auth_service
Username: postgres
Password: Niro
```

**Tables Created:**
- `roles` - User roles (ADMIN, USER, etc.)
- `permissions` - Permission definitions
- `role_permissions` - Many-to-many mapping
- `users` - Main user table with Firebase UID
- `user_roles` - User-role assignments
- `user_sessions` - Active user sessions
- `user_audit_log` - Audit trail

**Flyway Migrations:**
- ✅ V1__initial_schema.sql - All base tables
- ✅ V2__seed_data.sql - Default roles and permissions

### 2. Firebase Configuration

**Backend (Firebase Admin SDK):**
- File: `C:\SecureKeys\gear-up\firebase-service-account.json`
- Project ID: `gear-up-46adc`
- Purpose: Verify Firebase ID tokens from frontend
- Property: `app.firebase-configuration-file`

**Frontend (Firebase Client SDK):**
- Location: `GearUp-frontent/src/lib/firebase.ts`
- Purpose: User authentication (login/signup)
- Exports: `auth` instance for authentication

### 3. Spring Security Configuration

**Public Endpoints (No Auth Required):**
```
POST /api/v1/users/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
GET /actuator/health
GET /actuator/info
```

**Protected Endpoints (Requires Firebase Token):**
```
All other /api/v1/** endpoints
```

**Admin Only:**
```
/actuator/** (except health and info)
```

**Custom Filter:**
- `FirebaseAuthenticationFilter` - Validates Firebase tokens
- Configured as Spring bean in SecurityConfig

###4. Infrastructure Services

**Docker Services (Running):**
- Redis: localhost:6379
- RabbitMQ: localhost:5672 (Management: 15672)
- PostgreSQL: localhost:5434 (Local installation, NOT Docker)

**Eureka Server:**
- Status: ⚠️ Not started (optional for standalone testing)
- Port: 8761 (when started)
- Impact: Non-critical, service works independently

---

## 🚀 **How to Start the Service**

### Method 1: Maven (Recommended for Development)
```powershell
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend"
.\mvnw spring-boot:run -pl services/user-auth-service
```

### Method 2: From service directory
```powershell
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend\services\user-auth-service"
..\..\mvnw spring-boot:run
```

### Startup Verification
Look for these log messages:
```
✅ "HikariPool-1 - Start completed"
✅ "Successfully validated 2 migrations"
✅ "Initialized JPA EntityManagerFactory"
✅ "Filter 'firebaseAuthenticationFilter' configured for use"
✅ "Tomcat started on port 8082"
✅ "Started UserAuthServiceApplication in X.XXX seconds"
```

---

## 🧪 **Testing the Service**

### 1. Health Check
```powershell
curl http://localhost:8082/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

### 2. Register New User (No Auth Required)
```powershell
$body = @{
  firebaseUid = "test-firebase-uid-123"
  email = "test@example.com"
  firstName = "Test"
  lastName = "User"
  phoneNumber = "+1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/v1/users/register" -Method POST -Body $body -ContentType "application/json"
```

### 3. Login (Get JWT Token)
```powershell
$loginBody = @{
  email = "admin@gearup.com"
  password = "Admin@123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/v1/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
```

### 4. Check Database Data
```sql
-- Connect to PostgreSQL
psql -U postgres -h localhost -p 5434 -d as_user_auth_service

-- View default roles
SELECT * FROM roles;

-- View default permissions
SELECT * FROM permissions;

-- View users (after registration)
SELECT id, firebase_uid, email, first_name, last_name, account_status, created_at 
FROM users;
```

---

## 🔐 **Authentication Flow**

### Frontend to Backend Flow:

1. **User Logs In** (Frontend):
   ```typescript
   // GearUp-frontent/src/lib/firebase.ts
   import { signInWithEmailAndPassword } from 'firebase/auth';
   const userCredential = await signInWithEmailAndPassword(auth, email, password);
   const idToken = await userCredential.user.getIdToken();
   ```

2. **Send Token to Backend**:
   ```typescript
   // Include in Authorization header
   headers: {
     'Authorization': `Bearer ${idToken}`
   }
   ```

3. **Backend Validates Token**:
   - FirebaseAuthenticationFilter intercepts request
   - Verifies token using Firebase Admin SDK
   - Extracts user info (firebase_uid, email)
   - Sets Spring Security authentication context

4. **Backend Processes Request**:
   - Controller methods can access authenticated user
   - Check roles/permissions from database
   - Return protected data

---

## 📝 **Default Seed Data** (from V2 migration)

### Roles:
| ID | Name | Description |
|----|------|-------------|
| 1 | ADMIN | System administrator |
| 2 | USER | Regular user |
| 3 | DRIVER | Driver role |

### Default Admin User:
```
Email: admin@gearup.com
Firebase UID: admin-firebase-uid-001
Password: (Managed by Firebase - Admin@123)
Roles: ADMIN, USER
Status: ACTIVE
```

### Permissions:
- `USER_CREATE` - Create new users
- `USER_READ` - View user data
- `USER_UPDATE` - Modify users
- `USER_DELETE` - Delete users
- `ROLE_MANAGE` - Manage roles
- `PERMISSION_MANAGE` - Manage permissions

---

## ⚠️ **Known Issues & Warnings**

### 1. Eureka Connection Errors (Non-Critical)
```
Request execution failure with status code 401
DiscoveryClient - unable to refresh its cache
```

**Resolution**: These are harmless if you're not using service discovery. To fix:
```powershell
# Start Eureka Server
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend"
.\mvnw spring-boot:run -pl service-discovery
```

### 2. Firebase Service Account Warning
```
No Firebase service account found; skipping initialization
```

**Resolution**: Ensure file exists at `C:\SecureKeys\gear-up\firebase-service-account.json`
- File should already exist ✅
- Restart service if you just created it

### 3. Redis Repository Warnings
```
Spring Data Redis - Could not safely identify store assignment for repository candidate
```

**Resolution**: These are informational - repositories are correctly identified as JPA, not Redis

---

## 🔧 **Troubleshooting**

### Service Won't Start
1. **Check PostgreSQL is running:**
   ```powershell
   psql -U postgres -h localhost -p 5434 -l
   ```
   - If not running, start PostgreSQL service
   - Password should be "Niro"

2. **Check port 8082 is available:**
   ```powershell
   netstat -ano | findstr :8082
   ```
   - If in use, kill the process or change port

3. **Check Maven is working:**
   ```powershell
   .\mvnw --version
   ```

### Database Connection Errors
1. Verify password in `application.yml`:
   ```yaml
   spring:
     datasource:
       password: Niro  # Should match PostgreSQL password
   ```

2. Test connection manually:
   ```powershell
   psql -U postgres -h localhost -p 5434 -d as_user_auth_service
   ```

### Firebase Token Verification Fails
1. Check Firebase credentials file exists
2. Verify Frontend is using correct Firebase project (gear-up-46adc)
3. Ensure token hasn't expired (tokens expire after 1 hour)

---

## 🔄 **Frontend Integration**

### Frontend Configuration
**File**: `GearUp-frontent/src/lib/firebase.ts`

```typescript
// Frontend initializes Firebase Client SDK
const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: "gear-up-46adc",
  // ... other config
};

export const auth = getAuth(app);
```

### Authentication Examples

**Login:**
```typescript
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '@/lib/firebase';

const login = async (email: string, password: string) => {
  const userCredential = await signInWithEmailAndPassword(auth, email, password);
  const idToken = await userCredential.user.getIdToken();
  
  // Use token for API calls
  const response = await fetch('http://localhost:8082/api/v1/users/profile', {
    headers: {
      'Authorization': `Bearer ${idToken}`
    }
  });
};
```

**Signup:**
```typescript
import { createUserWithEmailAndPassword } from 'firebase/auth';

const signup = async (email: string, password: string, userData: any) => {
  // 1. Create Firebase user
  const userCredential = await createUserWithEmailAndPassword(auth, email, password);
  const idToken = await userCredential.user.getIdToken();
  
  // 2. Register in backend
  await fetch('http://localhost:8082/api/v1/users/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${idToken}`
    },
    body: JSON.stringify({
      firebaseUid: userCredential.user.uid,
      email: email,
      ...userData
    })
  });
};
```

---

## 📊 **Service Endpoints**

### Public Endpoints

#### Register User
```
POST /api/v1/users/register
Content-Type: application/json

{
  "firebaseUid": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "displayName": "string",
  "profileImageUrl": "string"
}
```

#### Login
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "string",
  "password": "string"
}

Response:
{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "roles": ["USER"]
  }
}
```

#### Refresh Token
```
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "string"
}
```

### Protected Endpoints (Require Firebase Token)

#### Get User Profile
```
GET /api/v1/users/profile
Authorization: Bearer <firebase-id-token>
```

#### Update User Profile
```
PUT /api/v1/users/profile
Authorization: Bearer <firebase-id-token>
Content-Type: application/json

{
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "displayName": "string",
  "profileImageUrl": "string"
}
```

#### Get User Roles
```
GET /api/v1/users/{userId}/roles
Authorization: Bearer <firebase-id-token>
```

---

## 🎯 **Next Steps**

### For Complete System Setup:

1. **Start Eureka Server** (Optional but recommended):
   ```powershell
   cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend"
   .\mvnw spring-boot:run -pl service-discovery
   ```

2. **Start Other Microservices**:
   - automobile-service
   - notification-service
   - template-service

3. **Start Frontend**:
   ```powershell
   cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-frontent"
   npm run dev
   ```

4. **Test Full Authentication Flow**:
   - Frontend signup/login
   - Backend token verification
   - Protected endpoint access

### For Production:

1. **Update Security Configuration**:
   - Remove generated security password warning
   - Configure proper JWT secrets
   - Set up CORS for specific origins

2. **Database**:
   - Use connection pooling settings
   - Set up database backups
   - Configure SSL connections

3. **Firebase**:
   - Verify production Firebase project
   - Update service account credentials
   - Configure proper security rules

4. **Monitoring**:
   - Enable all Actuator endpoints with security
   - Set up health checks
   - Configure logging levels

---

## 📞 **Support & References**

### Documentation Files:
- `/docs/QUICK_START.md` - General quickstart
- `/docs/DEV_GUIDE.md` - Development guide
- `/docs/POSTGRES_SETUP.md` - PostgreSQL setup

### Configuration Files:
- `services/user-auth-service/src/main/resources/application.yml`
- `.env` - Environment variables
- `C:\SecureKeys\gear-up\firebase-service-account.json`

### Migration Files:
- `services/user-auth-service/src/main/resources/db/migration/V1__initial_schema.sql`
- `services/user-auth-service/src/main/resources/db/migration/V2__seed_data.sql`

---

## ✅ **Verification Checklist**

- [x] PostgreSQL 18 installed and running on port 5434
- [x] Database `as_user_auth_service` created
- [x] Flyway migrations executed (V1, V2)
- [x] Firebase credentials file created
- [x] Spring Security configured with Firebase filter
- [x] Docker services running (Redis, RabbitMQ)
- [x] Service starts successfully on port 8082
- [ ] Eureka Server started (optional)
- [ ] Health endpoint tested
- [ ] Frontend integration tested
- [ ] Full auth flow verified

---

**Setup Completed By**: GitHub Copilot  
**Date**: November 6, 2025  
**Version**: 1.0.0  
**Status**: ✅ Ready for Testing
