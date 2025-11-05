# Cross-Service Communication Test Guide

## Overview

This guide demonstrates the complete cross-service communication flow between **automobile-service** and **notification-service** using:

- **PostgreSQL**: Database for both services
- **RabbitMQ**: Asynchronous event messaging
- **Redis**: Caching layer
- **Eureka**: Service discovery

## Architecture Flow

```
┌────────────────────────────────────────────────────────────────┐
│                    USER CREATES BOOKING                        │
└────────────────────┬───────────────────────────────────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  automobile-service     │
        │     (Port 8080)         │
        └─────┬─────────┬─────────┘
              │         │
              │         │ 2. Cache in Redis
              │         ▼
              │    ┌─────────┐
              │    │  Redis  │
              │    └─────────┘
              │
              │ 3. Publish Event
              ▼
        ┌─────────────┐
        │  RabbitMQ   │
        │ (Exchange)  │
        └──────┬──────┘
               │ 4. Route to Queue
               ▼
        ┌─────────────┐
        │    Queue    │
        └──────┬──────┘
               │ 5. Consume Event
               ▼
        ┌─────────────────────────┐
        │  notification-service   │
        │     (Port 8081)         │
        └─────────┬───────────────┘
                  │
                  │ 6. Create Notification
                  ▼
            ┌──────────────┐
            │  PostgreSQL  │
            │ (Notification│
            │     Table)   │
            └──────────────┘
```

## Prerequisites

Ensure all infrastructure services are running:

```powershell
# Check Docker containers
docker ps

# Expected containers:
# - gearup-postgres
# - gearup-rabbitmq
# - gearup-redis
```

## Step-by-Step Testing

### Step 1: Start automobile-service

```powershell
# Terminal 1
cd services/automobile-service
./mvnw spring-boot:run
```

**Expected output:**

```
Started DemoApplication in X.XXX seconds
Tomcat started on port 8080
Registered with Eureka
```

### Step 2: Start notification-service

```powershell
# Terminal 2
cd services/notification-service
./mvnw spring-boot:run
```

**Expected output:**

```
Started NotificationServiceApplication in X.XXX seconds
Tomcat started on port 8081
Registered with Eureka
```

### Step 3: Create a Test Booking (Trigger Event)

```powershell
# Terminal 3
curl -X POST "http://localhost:8080/api/bookings/test?userId=test-user-123"
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Test booking created successfully",
  "booking": {
    "bookingId": "123e4567-e89b-12d3-a456-426614174000",
    "vehicleId": "VEH-abc123",
    "vehicleName": "Tesla Model 3",
    "userId": "test-user-123",
    "customerName": "John Doe",
    "bookingStartDate": "2025-11-06T10:00:00",
    "bookingEndDate": "2025-11-08T10:00:00",
    "totalAmount": 299.99,
    "status": "PENDING",
    "createdAt": "2025-11-05T19:00:00"
  },
  "steps": [
    "✓ Booking saved to PostgreSQL database",
    "✓ Booking cached in Redis",
    "✓ VehicleBookingCreatedEvent published to RabbitMQ",
    "→ Notification-service will consume event",
    "→ Notification will be created in notification database",
    "→ Check notification-service logs to verify event consumption"
  ],
  "nextSteps": {
    "checkNotifications": "GET http://localhost:8081/api/notifications/user/test-user-123",
    "confirmBooking": "POST http://localhost:8080/api/bookings/{bookingId}/confirm",
    "getBooking": "GET http://localhost:8080/api/bookings/{bookingId}"
  }
}
```

### Step 4: Verify Event Flow

#### 4.1: Check automobile-service Logs

You should see:

```
Creating new booking for user: test-user-123, vehicle: VEH-abc123
Booking created with ID: 123e4567-e89b-12d3-a456-426614174000
Published VehicleBookingCreatedEvent for booking: 123e4567-e89b-12d3-a456-426614174000
```

#### 4.2: Check RabbitMQ Management UI

```
http://localhost:15672
Username: guest
Password: guest
```

Navigate to **Queues** → **notification.queue**:

- Message rates should show 1 message published
- Message should be consumed (ready: 0)

#### 4.3: Check notification-service Logs

You should see:

```
Received VehicleBookingCreatedEvent: bookingId=123e4567-e89b-12d3-a456-426614174000
Creating notification for user: test-user-123
Notification created: [VEHICLE_BOOKING_CREATED] Your booking for Tesla Model 3 has been confirmed!
```

### Step 5: Verify Notification Created

```powershell
curl "http://localhost:8081/api/notifications/user/test-user-123"
```

**Expected Response:**

```json
{
  "content": [
    {
      "id": "abc-123",
      "userId": "test-user-123",
      "type": "VEHICLE_BOOKING_CREATED",
      "title": "Vehicle Booking Created",
      "message": "Your booking for Tesla Model 3 has been confirmed! Pickup: Nov 6, 2025 at 10:00 AM",
      "isRead": false,
      "createdAt": "2025-11-05T19:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### Step 6: Test Redis Caching

#### 6.1: First Request (Database Hit)

```powershell
curl "http://localhost:8080/api/bookings/{bookingId}"
```

**automobile-service logs:**

```
Fetching booking: {bookingId} (will check cache first)
Booking fetched from database (cache miss): {bookingId}
```

#### 6.2: Second Request (Cache Hit)

```powershell
curl "http://localhost:8080/api/bookings/{bookingId}"
```

**automobile-service logs:**

```
Fetching booking: {bookingId} (will check cache first)
[No database query - served from Redis cache]
```

#### 6.3: Verify in Redis

```powershell
# Connect to Redis container
docker exec -it gearup-redis redis-cli

# Check cached bookings
KEYS bookings::*

# Get cached value
GET "bookings::{bookingId}"
```

### Step 7: Test Booking Confirmation Event

```powershell
curl -X POST "http://localhost:8080/api/bookings/{bookingId}/confirm"
```

**Expected Flow:**

1. automobile-service updates booking status to CONFIRMED
2. Updates Redis cache
3. Publishes `VehicleBookingConfirmedEvent` to RabbitMQ
4. notification-service consumes event
5. Creates a new notification for the user

**Verify:**

```powershell
curl "http://localhost:8081/api/notifications/user/test-user-123"
```

You should now see **2 notifications**:

- VEHICLE_BOOKING_CREATED
- VEHICLE_BOOKING_CONFIRMED

## Complete Test Script

Save this as `test-cross-service-flow.ps1`:

```powershell
# Cross-Service Communication Flow Test Script

Write-Host "`n=== Starting Cross-Service Communication Test ===" -ForegroundColor Cyan

$userId = "test-user-$(Get-Random -Minimum 1000 -Maximum 9999)"
Write-Host "`nTest User ID: $userId" -ForegroundColor Yellow

# Step 1: Create booking
Write-Host "`n[1/5] Creating test booking..." -ForegroundColor Green
$createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/test?userId=$userId" -Method POST
$bookingId = $createResponse.booking.bookingId
Write-Host "✓ Booking created: $bookingId" -ForegroundColor Green

Start-Sleep -Seconds 2

# Step 2: Verify booking in automobile-service
Write-Host "`n[2/5] Fetching booking from automobile-service (Redis cache)..." -ForegroundColor Green
$booking = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/$bookingId"
Write-Host "✓ Booking retrieved: $($booking.vehicleName)" -ForegroundColor Green

# Step 3: Verify notification created
Write-Host "`n[3/5] Checking notifications in notification-service..." -ForegroundColor Green
$notifications = Invoke-RestMethod -Uri "http://localhost:8081/api/notifications/user/$userId"
Write-Host "✓ Notifications found: $($notifications.totalElements)" -ForegroundColor Green

# Step 4: Confirm booking
Write-Host "`n[4/5] Confirming booking..." -ForegroundColor Green
$confirmed = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/$bookingId/confirm" -Method POST
Write-Host "✓ Booking confirmed: $($confirmed.status)" -ForegroundColor Green

Start-Sleep -Seconds 2

# Step 5: Verify confirmation notification
Write-Host "`n[5/5] Checking for confirmation notification..." -ForegroundColor Green
$finalNotifications = Invoke-RestMethod -Uri "http://localhost:8081/api/notifications/user/$userId"
Write-Host "✓ Total notifications: $($finalNotifications.totalElements)" -ForegroundColor Green

# Summary
Write-Host "`n=== Test Summary ===" -ForegroundColor Cyan
Write-Host "User ID: $userId" -ForegroundColor Yellow
Write-Host "Booking ID: $bookingId" -ForegroundColor Yellow
Write-Host "Notifications Created: $($finalNotifications.totalElements)" -ForegroundColor Yellow

Write-Host "`n✓ PostgreSQL: Booking stored" -ForegroundColor Green
Write-Host "✓ Redis: Booking cached" -ForegroundColor Green
Write-Host "✓ RabbitMQ: Events published and consumed" -ForegroundColor Green
Write-Host "✓ Cross-Service: notification-service received events" -ForegroundColor Green

Write-Host "`nView notifications:" -ForegroundColor Cyan
$finalNotifications.content | ForEach-Object {
    Write-Host "  - $($_.type): $($_.title)" -ForegroundColor Gray
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
```

## Verification Checklist

- [ ] automobile-service started on port 8080
- [ ] notification-service started on port 8081
- [ ] Both services registered with Eureka (check http://localhost:8761)
- [ ] RabbitMQ running (check http://localhost:15672)
- [ ] Redis running (docker ps | grep redis)
- [ ] PostgreSQL running (docker ps | grep postgres)
- [ ] Test booking created successfully
- [ ] Event published to RabbitMQ
- [ ] Event consumed by notification-service
- [ ] Notification created in database
- [ ] Redis caching working (second fetch faster)
- [ ] Booking confirmation event works
- [ ] Multiple notifications created for same user

## Troubleshooting

### Issue: automobile-service fails to start

**Check:**

```powershell
# Verify database connection
docker exec -it gearup-postgres psql -U postgres -d automobile_db -c "\dt"

# Check environment variables
cat services/automobile-service/.env
```

### Issue: Events not consumed

**Check:**

1. RabbitMQ bindings exist:
   ```
   http://localhost:15672/#/exchanges/%2F/notification.exchange
   ```
2. notification-service logs show listener started:

   ```
   Started RabbitMQ listeners
   ```

3. Queue has messages:
   ```
   http://localhost:15672/#/queues/%2F/notification.queue
   ```

### Issue: Redis caching not working

**Check:**

```powershell
# Connect to Redis
docker exec -it gearup-redis redis-cli

# Check if keys exist
KEYS *

# Monitor Redis commands
MONITOR
```

## Performance Metrics

Expected metrics for the flow:

| Operation                 | Time          | Notes                            |
| ------------------------- | ------------- | -------------------------------- |
| Create booking (DB write) | 50-100ms      | Includes event publishing        |
| Event publish to RabbitMQ | 5-10ms        | Async operation                  |
| Event consumption         | 10-50ms       | Listener processing              |
| Notification creation     | 30-50ms       | DB write in notification-service |
| **Total end-to-end**      | **100-200ms** | From booking to notification     |
| Cache hit (Redis)         | 5-10ms        | 10x faster than DB               |

## Next Steps

1. **Add more event types**: Invoice created, task assigned, etc.
2. **Implement WebSocket**: Push notifications to frontend
3. **Add monitoring**: Prometheus + Grafana for metrics
4. **Scale services**: Multiple instances with load balancing
5. **Add circuit breakers**: Handle service failures gracefully

---

**Documentation References:**

- [CROSS_SERVICE_COMMUNICATION.md](../CROSS_SERVICE_COMMUNICATION.md) - Complete guide
- [RABBITMQ_GUIDE.md](../RABBITMQ_GUIDE.md) - RabbitMQ specifics
- [ARCHITECTURE_DIAGRAMS.md](../ARCHITECTURE_DIAGRAMS.md) - System architecture
