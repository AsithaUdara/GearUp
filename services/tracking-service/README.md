# Tracking Service

Service Tracking and Progress Management Service for GearUp platform.

## Overview

This microservice handles:
- Employee work task management
- Service progress tracking
- Modification request management
- Parts/materials request handling
- Real-time progress updates for customers

## Features

- ✅ Task assignment and tracking for employees
- ✅ Progress step tracking (1-5 scale)
- ✅ Customer modification requests
- ✅ Parts/materials requests
- ✅ Daily summary reports
- ✅ Real-time notifications
- ✅ Service completion reports

## Tech Stack

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Flyway (Database Migrations)
- Lombok

## Prerequisites

- Java 21+
- PostgreSQL 14+
- Maven 3.8+

## Database Setup

```bash
# Create database
createdb gearup_tracking

# Run migrations (automatic on startup)
```

## Running Locally

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## API Endpoints

### Task Management
- `GET /api/tracking/employee/{employeeId}/tasks` - Get all tasks for employee
- `POST /api/tracking/tasks` - Create new task
- `PUT /api/tracking/tasks/{taskId}` - Update task progress
- `GET /api/tracking/tasks/{taskId}` - Get task details

### Progress Tracking
- `GET /api/tracking/service/{serviceId}/progress` - Get service progress
- `GET /api/tracking/vehicle/{vehicle}/services` - Get all services for vehicle

### Modification Requests
- `GET /api/tracking/employee/{employeeId}/modification-requests/pending` - Get pending requests
- `POST /api/tracking/modification-requests` - Create modification request
- `POST /api/tracking/modification-requests/{requestId}/create-task` - Create task from request

### Parts Requests
- `GET /api/tracking/vehicle/{vehicle}/parts-requests` - Get parts requests for vehicle
- `POST /api/tracking/parts-requests` - Create parts request

## Environment Variables

```properties
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=gearup_tracking
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
SERVER_PORT=8086
```

## Database Schema

### Tables
- `work_task` - Employee work tasks
- `modification_request` - Customer service modification requests
- `parts_request` - Parts and materials requests
- `service_progress` - Customer-facing service progress tracking

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## Docker

```bash
# Build image
docker build -t gearup/tracking-service:latest .

# Run container
docker run -p 8086:8086 \
  -e POSTGRES_HOST=postgres \
  -e POSTGRES_PASSWORD=yourpassword \
  gearup/tracking-service:latest
```

## Kubernetes Deployment

```bash
kubectl apply -f k8s/
```

## Contributing

See main repository CONTRIBUTING.md

## License

Proprietary - GearUp Platform
