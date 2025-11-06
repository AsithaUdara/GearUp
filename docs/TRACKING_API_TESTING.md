# Testing Tracking Service APIs

This guide explains how to verify that the Tracking Service APIs are working correctly.

## Prerequisites

1. **Services Running**: Ensure the tracking service and its dependencies are running
2. **Database**: PostgreSQL should be running and initialized
3. **Ports**: Tracking service runs on port `8086`

## Quick Start

### Step 1: Check if Services are Running

```powershell
# Check if tracking service container is running
docker ps | Select-String "tracking"

# Or check all services health
.\scripts\health-check.ps1
```

### Step 2: Run the API Test Script

```powershell
# Run comprehensive API tests
.\scripts\test-tracking-apis.ps1
```

This script will test:
- ✅ Service health endpoint
- ✅ Task CRUD operations (Create, Read)
- ✅ Employee endpoints (tasks, daily summary, modification requests)
- ✅ Modification request endpoints
- ✅ Parts request endpoints
- ✅ Progress endpoints

## Manual Testing

### Option 1: Using PowerShell (Invoke-WebRequest)

#### Test Health Endpoint
```powershell
Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -UseBasicParsing
```

#### Create a Task
```powershell
$body = @{
    serviceId = "svc-001"
    vehicle = "VEH-001"
    customer = "John Doe"
    serviceType = "Oil Change"
    assigneeId = "emp-001"
    estimatedDuration = 30
    notes = "Regular maintenance"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8086/api/tracking/tasks" `
    -Method POST `
    -Body $body `
    -ContentType "application/json" `
    -UseBasicParsing
```

#### Get Employee Tasks
```powershell
Invoke-WebRequest -Uri "http://localhost:8086/api/tracking/employee/emp-001/tasks" -UseBasicParsing
```

#### Get Daily Summary
```powershell
Invoke-WebRequest -Uri "http://localhost:8086/api/tracking/employee/emp-001/daily-summary" -UseBasicParsing
```

### Option 2: Using cURL (if available)

```bash
# Health check
curl http://localhost:8086/actuator/health

# Create task
curl -X POST http://localhost:8086/api/tracking/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": "svc-001",
    "vehicle": "VEH-001",
    "customer": "John Doe",
    "serviceType": "Oil Change",
    "assigneeId": "emp-001",
    "estimatedDuration": 30,
    "notes": "Regular maintenance"
  }'

# Get employee tasks
curl http://localhost:8086/api/tracking/employee/emp-001/tasks
```

### Option 3: Using Postman or Insomnia

1. **Base URL**: `http://localhost:8086`
2. **API Base Path**: `/api/tracking`

#### Example Requests:

**GET** `/api/tracking/employee/{employeeId}/tasks`
- Example: `/api/tracking/employee/emp-001/tasks`

**POST** `/api/tracking/tasks`
- Body (JSON):
```json
{
  "serviceId": "svc-001",
  "vehicle": "VEH-001",
  "customer": "John Doe",
  "serviceType": "Oil Change",
  "assigneeId": "emp-001",
  "estimatedDuration": 30,
  "notes": "Regular maintenance"
}
```

**POST** `/api/tracking/modification-requests`
- Body (JSON):
```json
{
  "serviceId": "svc-002",
  "vehicle": "VEH-002",
  "customer": "Jane Smith",
  "type": "UPGRADE",
  "title": "Engine Upgrade",
  "description": "Upgrade engine components",
  "requestedBy": "customer-001",
  "assignedToEmployeeId": "emp-001",
  "estimatedCost": 500.00,
  "estimatedDuration": 120
}
```

**POST** `/api/tracking/parts-requests`
- Body (JSON):
```json
{
  "material": "Engine Oil",
  "quantity": 5,
  "status": "PENDING",
  "date": "2024-01-15",
  "vehicle": "VEH-001",
  "serviceId": "svc-001",
  "requestedBy": "emp-001",
  "notes": "Standard oil change",
  "cost": 50.00
}
```

## API Endpoints Reference

### Task Endpoints
- `POST /api/tracking/tasks` - Create task
- `GET /api/tracking/tasks/{taskId}` - Get task by ID
- `PUT /api/tracking/tasks/{taskId}` - Update task
- `GET /api/tracking/tasks/vehicle/{vehicle}` - Get tasks by vehicle
- `GET /api/tracking/tasks/service/{serviceId}` - Get tasks by service ID

### Employee Endpoints
- `GET /api/tracking/employee/{employeeId}/tasks` - Get employee tasks
- `GET /api/tracking/employee/{employeeId}/daily-summary` - Get daily summary
- `GET /api/tracking/employee/{employeeId}/modification-requests/pending` - Get pending requests

### Modification Request Endpoints
- `POST /api/tracking/modification-requests` - Create modification request
- `GET /api/tracking/vehicle/{vehicle}/modification-requests` - Get by vehicle
- `POST /api/tracking/modification-requests/{requestId}/create-task` - Create task from request

### Parts Request Endpoints
- `POST /api/tracking/parts-requests` - Create parts request
- `GET /api/tracking/vehicle/{vehicle}/parts-requests` - Get by vehicle
- `GET /api/tracking/service/{serviceId}/parts-requests` - Get by service ID

### Progress Endpoints
- `GET /api/tracking/progress/service/{serviceId}` - Get service progress
- `PUT /api/tracking/progress/service/{serviceId}?currentStep={step}` - Update progress

## Troubleshooting

### Service Not Responding
```powershell
# Check if container is running
docker ps

# Check logs
docker logs gearup-tracking-service

# Restart service
docker compose -f deployment/docker/docker-compose.yml restart tracking-service
```

### Database Connection Issues
```powershell
# Test database connection
.\scripts\test-db-connections.ps1

# Check if database exists
docker exec -it gearup-postgres psql -U postgres -d as_tracking_service -c "\dt"
```

### Port Already in Use
```powershell
# Check what's using port 8086
netstat -ano | findstr :8086

# Or use PowerShell
Get-NetTCPConnection -LocalPort 8086
```

### Common HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error (check logs)

## Next Steps

After verifying APIs are working:
1. ✅ Test with real data
2. ✅ Test error scenarios
3. ✅ Test authentication (if Firebase auth is configured)
4. ✅ Test through API Gateway (if gateway is running)
5. ✅ Check database to verify data persistence

## Additional Resources

- Service logs: `docker logs gearup-tracking-service -f`
- Database access: `docker exec -it gearup-postgres psql -U postgres -d as_tracking_service`
- API Gateway: `http://localhost:8080` (if running)

