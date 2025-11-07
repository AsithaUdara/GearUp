# GearUp Backend API Documentation

## Base URL

- **Development**: `http://localhost:9090`
- **Production**: `https://your-domain.com`

## Authentication

### Public Endpoints (No Authentication Required)

- `/api/chat/**` - Chatbot endpoints
- `/actuator/health` - Health check (all services)
- `/actuator/info` - Service information (all services)

### Protected Endpoints (Firebase Authentication Required)

All other endpoints require Firebase JWT token in the `Authorization` header:

```
Authorization: Bearer <firebase-jwt-token>
```

## Services

### 1. API Gateway (`http://localhost:9090`)

**Purpose**: Single entry point for all microservices

**Responsibilities**:

- Route requests to appropriate microservices
- Load balancing via Eureka service discovery
- CORS handling
- Request/Response filtering

### 2. Service Discovery (`http://localhost:8761`)

**Purpose**: Eureka server for service registration and discovery

**Admin Access**:

- **URL**: `http://localhost:8761`
- **Username**: `admin`
- **Password**: `password`

**Note**: Only for admin/developer use. Public users never access Eureka directly.

### 3. Chatbot Service (`http://localhost:8084`)

**Purpose**: AI-powered chatbot for customer support

**Endpoints**:

- `POST /api/chat/message` - Send chat message (PUBLIC)
- `GET /api/chat/sessions` - Get chat sessions (PUBLIC)
- `POST /api/chat/session` - Create new session (PUBLIC)
- `WS /ws/chat` - WebSocket connection (PUBLIC)

**Public Access**: ✅ All endpoints are public - no authentication required

### 4. Automobile Service (`http://localhost:8080`)

**Purpose**: Vehicle listing and management

**Endpoints**:

- `GET /api/automobiles` - List all vehicles (PROTECTED)
- `GET /api/automobiles/{id}` - Get vehicle details (PROTECTED)
- `POST /api/automobiles` - Create vehicle listing (PROTECTED)
- `PUT /api/automobiles/{id}` - Update vehicle (PROTECTED)
- `DELETE /api/automobiles/{id}` - Delete vehicle (PROTECTED)

**Public Access**: ❌ Requires Firebase authentication

### 5. Notification Service (`http://localhost:8081`)

**Purpose**: Push notifications and alerts

**Endpoints**:

- `POST /api/notifications` - Send notification (PROTECTED)
- `GET /api/notifications/user/{userId}` - Get user notifications (PROTECTED)
- `WS /ws/notifications` - WebSocket for real-time notifications (PUBLIC)

**Public Access**: ⚠️ WebSocket public, API endpoints protected

### 6. User Auth Service (`http://localhost:8082`)

**Purpose**: User authentication and authorization

**Endpoints**:

- `POST /api/users/register` - Register new user (PROTECTED)
- `POST /api/users/login` - User login (PROTECTED)
- `GET /api/users/profile` - Get user profile (PROTECTED)
- `PUT /api/users/profile` - Update profile (PROTECTED)

**Public Access**: ❌ Requires Firebase authentication

## Request/Response Examples

### Chatbot - Send Message (PUBLIC)

```bash
curl -X POST http://localhost:9090/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session-123",
    "message": "What vehicles do you have?"
  }'
```

**Response**:

```json
{
  "sessionId": "session-123",
  "message": "We have a wide range of vehicles including sedans, SUVs, and trucks.",
  "timestamp": "2025-11-06T14:30:00Z"
}
```

### Automobiles - List Vehicles (PROTECTED)

```bash
curl -X GET http://localhost:9090/api/automobiles \
  -H "Authorization: Bearer <your-firebase-token>"
```

**Response**:

```json
{
  "vehicles": [
    {
      "id": "1",
      "make": "Toyota",
      "model": "Camry",
      "year": 2024,
      "price": 28000
    }
  ]
}
```

## CORS Configuration

All services allow:

- **Origins**: `http://localhost:3000`, `http://localhost:3001`
- **Methods**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`
- **Headers**: `*` (all headers)
- **Credentials**: Varies by service

## Frontend Integration

### Step 1: Connect to API Gateway

```javascript
const API_BASE_URL = "http://localhost:9090";
```

### Step 2: Public Chatbot (No Auth)

```javascript
async function sendChatMessage(message) {
  const response = await fetch(`${API_BASE_URL}/api/chat/message`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      sessionId: localStorage.getItem("chatSessionId"),
      message: message,
    }),
  });
  return response.json();
}
```

### Step 3: Protected Endpoints (With Firebase Auth)

```javascript
import { getAuth } from "firebase/auth";

async function getVehicles() {
  const auth = getAuth();
  const user = auth.currentUser;
  const token = await user.getIdToken();

  const response = await fetch(`${API_BASE_URL}/api/automobiles`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
  return response.json();
}
```

## Error Handling

### Common HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error
- `503 Service Unavailable` - Service not registered or down

### Error Response Format

```json
{
  "timestamp": "2025-11-06T14:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid Firebase token",
  "path": "/api/automobiles"
}
```

## Health Monitoring

### Check Service Health

```bash
# API Gateway
curl http://localhost:9090/actuator/health

# Individual Service
curl http://localhost:8080/actuator/health
```

**Response**:

```json
{
  "status": "UP"
}
```

### View Registered Services (Eureka Dashboard)

1. Open `http://localhost:8761`
2. Login with `admin` / `password`
3. View all registered services

## WebSocket Connections

### Chatbot WebSocket

```javascript
const ws = new WebSocket("ws://localhost:8084/ws/chat");

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log("Received:", message);
};

ws.send(
  JSON.stringify({
    type: "chat",
    message: "Hello!",
  })
);
```

### Notifications WebSocket

```javascript
const ws = new WebSocket("ws://localhost:8081/ws/notifications");

ws.onmessage = (event) => {
  const notification = JSON.parse(event.data);
  // Display notification to user
};
```

## Rate Limiting

- **User Auth Routes**: 10 requests/second, burst capacity 20
- Other routes: No rate limiting (subject to change in production)

## Support

For API issues or questions:

- Check service health endpoints
- Review Eureka dashboard for service registration
- Check Docker logs: `docker logs <container-name>`
