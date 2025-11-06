# Payment Service

Payment Service for GearUp backend - handles payment requests and customer bills management.

## Overview

This microservice manages the complete payment workflow:
- **Payment Requests**: Employees/technicians submit payment requests for services performed
- **Admin Approval**: Admins review and approve/reject payment requests
- **Customer Bills**: Approved requests automatically generate customer bills
- **Payment Tracking**: Customers can view bills, mark as paid, and submit reviews

## Technology Stack

- **Java 21** (Eclipse Temurin)
- **Spring Boot 3.x**
- **Spring Data JPA** (Hibernate)
- **PostgreSQL 15**
- **Flyway** (Database migrations)
- **Lombok** (Reduce boilerplate)
- **Jakarta Validation** (Bean validation)

## Port

**8083** - HTTP REST API

## Database

- **Database Name**: `as_payment_service`
- **User**: `svc_payment_service`
- **Schema**: Managed by Flyway migrations

### Tables

1. **payment_requests** - Stores payment requests from employees
2. **payment_request_services** - Individual service items for each request
3. **customer_bills** - Generated bills for approved requests

## API Endpoints

### Admin APIs (`/api/payments/admin`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/requests` | Create new payment request |
| GET | `/requests` | Get all payment requests |
| GET | `/requests?status=PENDING` | Filter by status |
| GET | `/requests/{id}` | Get specific request |
| PUT | `/requests/{id}/approve` | Approve request |
| PUT | `/requests/{id}/reject` | Reject request |
| GET | `/stats` | Get payment statistics |

### Customer APIs (`/api/payments/customer`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/bills?email={email}` | Get customer bills |
| GET | `/bills/{id}` | Get specific bill |
| GET | `/bills?status=UNPAID` | Filter by payment status |
| PUT | `/bills/{id}/mark-paid` | Mark bill as paid |

## Mock Data

The service includes seed data for testing (enabled in `dev` profile):

- **3 Pending** payment requests
- **2 Approved** requests (with bills)
- **1 Rejected** request
- **1 Paid** bill with review

## Quick Start

### 1. Build the Service

From repository root:

```powershell
.\mvnw.cmd clean package -DskipTests -pl services/payment-service -am
```

### 2. Run with Docker Compose

```powershell
cd deployment/docker
docker-compose up -d payment-service
```

### 3. Verify Service is Running

```powershell
# Health check
curl http://localhost:8083/actuator/health

# Get all payment requests
curl http://localhost:8083/api/payments/admin/requests

# Get customer bills
curl "http://localhost:8083/api/payments/customer/bills?email=jane@example.com"
```

## Development

### Run Locally (without Docker)

```powershell
cd services/payment-service
..\..\mvnw.cmd spring-boot:run
```

**Prerequisites:**
- PostgreSQL running on localhost:5432
- Database `as_payment_service` created
- User `svc_payment_service` with permissions

### Environment Variables

Set in `.env` file (repository root):

```properties
PAYMENT_DB_URL=jdbc:postgresql://db:5432/as_payment_service
PAYMENT_DB_USER=svc_payment_service
PAYMENT_DB_PASSWORD=payment_svc_pass_2024
```

### Database Migrations

Flyway automatically runs migrations on startup.

**Migration Files Location:**
```
src/main/resources/db/migration/
├── V1__initial_schema.sql     # Tables, indexes, triggers
└── V2__seed_data.sql           # Mock data for testing
```

**Run migrations manually:**
```powershell
.\mvnw.cmd flyway:migrate -pl services/payment-service
```

## Testing

### With Postman

Import collection from: `docs/postman/Payment-Service-API.postman_collection.json`

### With cURL

**Create Payment Request:**
```bash
curl -X POST http://localhost:8083/api/payments/admin/requests \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test Customer",
    "customerEmail": "test@example.com",
    "vehicleInfo": "Tesla Model 3 2023 - TEST123",
    "services": [
      {"description": "Battery Check", "price": 75.00},
      {"description": "Software Update", "price": 150.00}
    ],
    "submittedBy": "Alex (Technician)",
    "submittedDate": "2025-11-06"
  }'
```

**Get All Requests:**
```bash
curl http://localhost:8083/api/payments/admin/requests
```

**Approve a Request:**
```bash
curl -X PUT http://localhost:8083/api/payments/admin/requests/{requestId}/approve
```

**Get Customer Bills:**
```bash
curl "http://localhost:8083/api/payments/customer/bills?email=jane@example.com"
```

## Logs

View service logs:

```powershell
docker logs gearup-payment-service -f
```

## Database Access

### Using psql

```bash
docker exec -it gearup-postgres psql -U svc_payment_service -d as_payment_service
```

### Using pgAdmin

Open http://localhost:5050
- Email: admin@gearup.com
- Password: admin123

Add server:
- Host: db
- Port: 5432
- Database: as_payment_service
- Username: svc_payment_service
- Password: payment_svc_pass_2024

## Business Logic

### Payment Request Flow

1. **Employee Submits** payment request with service items
2. **Admin Reviews** request in admin panel
3. **Admin Approves** → System automatically creates customer bill with 10% tax
4. **Customer Views** bill in their dashboard
5. **Customer Pays** → Marks bill as paid
6. **Customer Reviews** → Can submit service review

### Status Transitions

**Payment Request:**
- `PENDING` → `APPROVED` (admin approves)
- `PENDING` → `REJECTED` (admin rejects)

**Customer Bill:**
- `UNPAID` → `PAID` (customer pays)

## Configuration

### Application Properties

Located in: `src/main/resources/application.properties`

Key configurations:
- Server port: 8083
- Database connection
- Flyway settings
- JPA/Hibernate settings
- Actuator endpoints

### Tax Calculation

Tax rate: **10%** (configurable in application.properties)

```
Subtotal = Sum of service prices
Tax = Subtotal × 0.10
Final Amount = Subtotal + Tax
```

## Troubleshooting

### Service won't start

1. Check PostgreSQL is running:
   ```powershell
   docker ps | grep postgres
   ```

2. Verify database exists:
   ```bash
   docker exec -it gearup-postgres psql -U postgres -c "\l"
   ```

3. Check logs:
   ```powershell
   docker logs gearup-payment-service
   ```

### Database connection errors

- Verify `.env` has correct credentials
- Check init-db.sql ran successfully
- Ensure service user has permissions

### No mock data

- Confirm `SPRING_PROFILES_ACTIVE=dev` in environment
- Check Flyway migration V2 ran successfully
- View Flyway history:
  ```bash
  docker exec -it gearup-postgres psql -U svc_payment_service -d as_payment_service -c "SELECT * FROM flyway_schema_history;"
  ```

## Project Structure

```
payment-service/
├── src/
│   ├── main/
│   │   ├── java/com/gearup/paymentservice/
│   │   │   ├── PaymentServiceApplication.java
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access
│   │   │   ├── model/               # JPA entities
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── enums/               # Enumerations
│   │   │   └── exception/           # Custom exceptions
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway SQL scripts
│   └── test/
├── target/                          # Build output
├── Dockerfile                       # Container definition
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

## Dependencies

See `pom.xml` for complete list. Key dependencies:

- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-validation` - Input validation
- `spring-boot-starter-actuator` - Health checks
- `postgresql` - Database driver
- `flyway-core` - Database migrations
- `lombok` - Code generation

## Contributing

1. Follow existing code patterns
2. Write tests for new features
3. Update this README for significant changes
4. Run `mvn clean verify` before committing

## Support

For issues or questions:
- Check logs first
- Review database state
- Test with Postman collection
- Contact backend team

---

**Service Status:** ✅ Ready for development and testing  
**Last Updated:** November 6, 2025
