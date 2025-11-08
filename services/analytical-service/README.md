# Analytical Service

Analytics and Reporting Service for GearUp platform.

## Overview

This microservice handles:
- Service analytics and performance metrics
- Revenue tracking and financial analytics
- Employee performance monitoring
- Customer behavior analysis
- Popular services and trending data
- Dashboard metrics and KPIs
- Business intelligence reports

## Features

- ✅ Service completion analytics
- ✅ Revenue tracking (daily, monthly, yearly)
- ✅ Employee performance metrics
- ✅ Customer lifetime value analysis
- ✅ Popular services trending
- ✅ Real-time dashboard data
- ✅ Export reports (CSV, PDF)

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
createdb as_analytical_service

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

### Analytics
- `GET /api/analytics/dashboard` - Get dashboard summary
- `GET /api/analytics/revenue/daily` - Daily revenue metrics
- `GET /api/analytics/revenue/monthly` - Monthly revenue metrics
- `GET /api/analytics/services/popular` - Popular services list

### Employee Analytics
- `GET /api/analytics/employees/{employeeId}/performance` - Employee performance
- `GET /api/analytics/employees/top` - Top performing employees

### Customer Analytics
- `GET /api/analytics/customers/{customerId}/summary` - Customer analytics
- `GET /api/analytics/customers/lifetime-value` - Customer LTV analysis

### Reports
- `GET /api/analytics/reports/services` - Service analytics report
- `GET /api/analytics/reports/revenue` - Revenue report

## Environment Variables

```properties
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=as_analytical_service
POSTGRES_USER=svc_analytical_service
POSTGRES_PASSWORD=analytical_svc_pass_2024
SERVER_PORT=8087
```

## Database Schema

### Tables
- `service_analytics` - Service completion metrics
- `revenue_analytics` - Daily revenue tracking
- `employee_performance` - Employee KPIs
- `customer_analytics` - Customer behavior data
- `popular_services` - Trending services

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
docker build -t gearup/analytical-service:latest .

# Run container
docker run -p 8087:8087 \
  -e POSTGRES_HOST=postgres \
  -e POSTGRES_PASSWORD=yourpassword \
  gearup/analytical-service:latest
```

## Kubernetes Deployment

```bash
kubectl apply -f k8s/
```

## Analytics Data Flow

1. Services emit events upon completion
2. Analytical service consumes events
3. Data is aggregated and stored
4. Analytics endpoints serve processed data
5. Dashboards display real-time metrics

## Contributing

See main repository CONTRIBUTING.md

## License

Proprietary - GearUp Platform
