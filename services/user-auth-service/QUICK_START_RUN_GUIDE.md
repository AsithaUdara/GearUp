# Quick Start - Run User Auth Service

## ✅ Prerequisites Check

Before starting, verify:
- [x] Java 21 installed
- [x] Maven installed
- [x] Docker running
- [x] Git repository cloned
- [x] Firebase service account JSON ready

## 🚀 Step-by-Step Startup

### Step 1: Start Infrastructure Services

```powershell
# Navigate to project root
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend"

# Start PostgreSQL, Redis, RabbitMQ using Docker Compose
cd deployment/docker
docker-compose up -d postgres redis rabbitmq

# Verify services are running
docker ps
```

Expected output:
```
CONTAINER ID   IMAGE          STATUS         PORTS
xxxxx          postgres:16    Up 10 seconds  0.0.0.0:5432->5432/tcp
xxxxx          redis:7        Up 10 seconds  0.0.0.0:6379->6379/tcp
xxxxx          rabbitmq:3     Up 10 seconds  0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
```

### Step 2: Start Config Server

```powershell
# Open new terminal
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend\config-server"
.\mvnw spring-boot:run
```

Wait for:
```
Started ConfigServerApplication in X.XXX seconds
```

**Config Server running on http://localhost:8888** ✅

### Step 3: Start Service Discovery (Eureka)

```powershell
# Open new terminal
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend\service-discovery"
.\mvnw spring-boot:run
```

Wait for:
```
Started ServiceDiscoveryApplication in X.XXX seconds
```

**Eureka Dashboard: http://localhost:8761** ✅

### Step 4: Place Firebase Service Account JSON

```powershell
# Create config directory
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend\services\user-auth-service"
mkdir config

# Copy your Firebase service account JSON
# Rename it to: gear-up-firebase-adminsdk.json
# Place it in: services/user-auth-service/config/
```

Or set environment variable:
```powershell
$env:FIREBASE_CONFIG_PATH="C:\path\to\your\firebase-adminsdk.json"
```

### Step 5: Start User Auth Service

```powershell
# Navigate to user-auth-service
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend\services\user-auth-service"

# Start the service
.\mvnw spring-boot:run
```

**Expected startup sequence:**
```
1. Loading application configuration from Config Server...
2. Connecting to PostgreSQL database...
3. Running Flyway migrations (V1, V2)...
4. Initializing Firebase Admin SDK...
5. Registering with Eureka...
6. Started UserAuthServiceApplication in X.XXX seconds
```

**User Auth Service running on http://localhost:8082** ✅

### Step 6: Verify Service Health

```powershell
# Test health endpoint
curl http://localhost:8082/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Step 7: Check Eureka Registration

Open browser: http://localhost:8761

You should see:
```
Application          AMIs        Availability Zones    Status
USER-AUTH-SERVICE    n/a         (1)                   UP (1) - localhost:user-auth-service:8082
```

---

## 🧪 Test the Service

### Test 1: Register a User

```powershell
# Register a new user
curl -X POST http://localhost:8082/api/v1/users/register `
  -H "Content-Type: application/json" `
  -d '{
    "firebaseUid": "test_user_001",
    "email": "test@example.com",
    "displayName": "Test User",
    "phoneNumber": "+1234567890",
    "role": "CUSTOMER"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "firebaseUid": "test_user_001",
    "email": "test@example.com",
    "displayName": "Test User",
    "accountStatus": "ACTIVE",
    "roles": [
      {
        "name": "CUSTOMER",
        "permissions": [ ... ]
      }
    ]
  }
}
```

### Test 2: Check Database

```powershell
# Connect to PostgreSQL
docker exec -it <postgres_container_id> psql -U svc_user_auth_service -d as_user_auth_service

# Check users table
SELECT id, email, display_name, account_status FROM users;

# Check roles
SELECT * FROM roles;

# Check user_roles mapping
SELECT u.email, r.name FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id;

# Exit
\q
```

### Test 3: Get All Roles (Requires Admin Token)

```powershell
# This will return 401 Unauthorized (as expected)
curl -X GET http://localhost:8082/api/v1/roles
```

Response:
```json
{
  "success": false,
  "message": "Unauthorized: Full authentication is required to access this resource",
  "timestamp": "2025-11-05T23:45:00"
}
```

---

## 🔧 Troubleshooting

### Issue: Config Server connection failed
**Symptom**: `Connect to localhost:8888 failed`

**Solution**:
```powershell
# Verify Config Server is running
netstat -an | findstr "8888"

# Restart Config Server
cd config-server
.\mvnw spring-boot:run
```

### Issue: PostgreSQL connection refused
**Symptom**: `Connection refused: localhost:5432`

**Solution**:
```powershell
# Check Docker containers
docker ps | findstr postgres

# If not running, start it
cd deployment/docker
docker-compose up -d postgres

# Check logs
docker logs <postgres_container_id>
```

### Issue: Flyway migration failed
**Symptom**: `FlywayException: Unable to obtain connection`

**Solution**:
```powershell
# Verify database credentials in config-repo/user-auth-service.yml
# Reset database
docker exec -it <postgres_container_id> psql -U postgres
DROP DATABASE as_user_auth_service;
CREATE DATABASE as_user_auth_service OWNER svc_user_auth_service;
\q

# Restart service
```

### Issue: Firebase initialization failed
**Symptom**: `FirebaseApp initialization failed`

**Solution**:
1. Verify Firebase JSON path: `services/user-auth-service/config/gear-up-firebase-adminsdk.json`
2. Check file permissions
3. Verify JSON format is valid
4. Ensure internet connectivity (Firebase needs to connect to Google servers)

### Issue: Eureka registration failed
**Symptom**: `Eureka registration timeout`

**Solution**:
```powershell
# Verify Eureka is running
netstat -an | findstr "8761"

# Restart Eureka
cd service-discovery
.\mvnw spring-boot:run
```

---

## 📊 Service Ports Reference

| Service | Port | URL | Status Endpoint |
|---------|------|-----|-----------------|
| Config Server | 8888 | http://localhost:8888 | /actuator/health |
| Eureka Server | 8761 | http://localhost:8761 | /actuator/health |
| **User Auth Service** | **8082** | **http://localhost:8082** | **/actuator/health** |
| PostgreSQL | 5432 | localhost:5432 | - |
| Redis | 6379 | localhost:6379 | - |
| RabbitMQ | 5672 (AMQP) | localhost:5672 | - |
| RabbitMQ UI | 15672 | http://localhost:15672 | guest/guest |

---

## 🎯 Next: Frontend Integration

Now that the service is running, integrate it with your frontend:

### 1. Firebase Authentication (Frontend)
```javascript
// Initialize Firebase in your React/Angular/Vue app
import { initializeApp } from 'firebase/app';
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';

const firebaseConfig = { /* your config */ };
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// Sign in user
const userCredential = await signInWithEmailAndPassword(auth, email, password);
const firebaseIdToken = await userCredential.user.getIdToken();
```

### 2. Backend Authentication
```javascript
// Send Firebase token to backend
const response = await fetch('http://localhost:8082/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    firebaseToken: firebaseIdToken,
    deviceInfo: navigator.userAgent,
    ipAddress: '192.168.1.100' // Get from request
  })
});

const { data } = await response.json();
const { accessToken, refreshToken } = data;

// Store tokens
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
```

### 3. Authenticated API Calls
```javascript
// Add Authorization header to all API calls
const getUserProfile = async () => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('http://localhost:8082/api/v1/users/me', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.json();
};
```

---

## 🎉 Success!

Your User Auth Service is now running and ready to handle:
- ✅ User registration
- ✅ Firebase authentication
- ✅ JWT token generation
- ✅ Role-based access control
- ✅ Session management
- ✅ Audit logging

**Service URL: http://localhost:8082**

**Swagger UI (if enabled): http://localhost:8082/swagger-ui.html**

---

## 📚 Additional Resources

- [IMPLEMENTATION_COMPLETE.md](./IMPLEMENTATION_COMPLETE.md) - Full implementation summary
- [AUTH_SERVICE_START_HERE.md](./AUTH_SERVICE_START_HERE.md) - Comprehensive guide
- [AUTH_SERVICE_QUICK_REFERENCE.md](./AUTH_SERVICE_QUICK_REFERENCE.md) - API reference
- [config-repo/user-auth-service.yml](../../config-repo/user-auth-service.yml) - Configuration

---

**Happy Coding! 🚀**
