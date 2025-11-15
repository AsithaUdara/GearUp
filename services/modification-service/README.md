# Modification Service

Vehicle modification request management service for GearUp platform.

## Overview

Allows customers to submit modification requests for their registered vehicles. Admins can review, approve, and track these requests through various status stages.

## Features

- Create modification requests
- Track request status (pending → approved → in_progress → completed/rejected)
- Query by user, vehicle, or status
- RabbitMQ event publishing for notifications
- Eureka service discovery integration

## Database

**Table**: `modifications`

**Columns**:
- `id` (UUID, PK)
- `user_id` (VARCHAR(64))
- `vehicle_id` (VARCHAR(255))
- `vehicle_label` (VARCHAR(500), optional)
- `subject` (VARCHAR(500))
- `message` (TEXT)
- `status` (VARCHAR(50), CHECK constraint)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**Statuses**: `pending`, `approved`, `in_progress`, `completed`, `rejected`

## API Endpoints

### Create Modification Request
```
POST /api/modifications
Headers: X-User-ID (optional, can be in body)
Body: {
  "userId": "firebase-uid",
  "vehicleId": "vehicle-uuid",
  "vehicleLabel": "2025 Toyota GR Hilux — CBE-2938",
  "subject": "TOYOTA HILUX ROOF MOUNTING KIT",
  "message": "I would like to install a roof mounting kit..."
}
```

### Get User's Modifications
```
GET /api/modifications/user/{userId}
```

### Get Vehicle's Modifications
```
GET /api/modifications/vehicle/{vehicleId}
```

### Get Single Modification
```
GET /api/modifications/{id}
```

### Update Status (Admin)
```
PATCH /api/modifications/{id}/status?status=approved
```

### Delete Modification
```
DELETE /api/modifications/{id}
```

## Configuration

### Environment Variables

- `SERVER_PORT`: Service port (default: 8087)
- `SPRING_DATASOURCE_URL`: Database connection string
- `SPRING_DATASOURCE_USERNAME`: Database user
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `RABBITMQ_HOST`: RabbitMQ host (default: localhost)
- `RABBITMQ_PORT`: RabbitMQ port (default: 5672)
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: Eureka server URL

## Events Published

- `modification.created`: When new request is created
- `modification.status.changed`: When status is updated
- `modification.deleted`: When request is deleted

All events published to `modification.exchange` topic exchange.

## Running Locally

```bash
# With Maven
mvn spring-boot:run

# With Docker
docker build -t modification-service .
docker run -p 8087:8087 modification-service
```

## Health Check

```
GET /actuator/health
```

## Port

Default: **8087** (configurable via SERVER_PORT)
