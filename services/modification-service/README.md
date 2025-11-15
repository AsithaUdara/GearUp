# Modification Service

The Modification Service handles all vehicle modification requests for the GearUp platform. It provides REST APIs for managing modification services, customer requests, and tracking modification status.

## Features

- **Service Management**: List available modification services
- **Request Management**: Create, retrieve, update, and delete modification requests
- **Customer Tracking**: Track services per customer
- **Status Workflow**: Manage request lifecycle (Pending → Approved/Rejected → In Progress → Completed)

## API Endpoints

### Service Modifications
- `GET /api/service-modifications/:serviceId` - Fetch one service + all related modification data
- `GET /api/service-modifications/:serviceId/refresh` - Refresh latest service info
- `POST /api/service-modifications/:serviceId/requests` - Submit new modification request
- `GET /api/service-modifications/:serviceId/requests` - List all modification requests for a service

### Modification Requests
- `GET /api/modification-requests/:requestId` - Get single modification request
- `PATCH /api/modification-requests/:requestId` - Update modification (approve/reject/notes)
- `DELETE /api/modification-requests/:requestId` - Delete modification (optional)

### Customers
- `GET /api/customers/:customerId/services` - List all services per customer

## Database Schema

### Tables
- **services**: Available modification services (engine upgrades, body kits, etc.)
- **customers**: Customer information
- **modification_requests**: Vehicle modification requests with status tracking

### Sample Services
The service comes pre-populated with sample modification services:
- Engine Performance Upgrade ($1,500, 8 hours)
- Suspension Modification ($1,200, 6 hours)
- Exhaust System Upgrade ($800, 4 hours)
- Body Kit Installation ($2,000, 12 hours)
- Interior Customization ($1,000, 10 hours)
- Lighting System Upgrade ($500, 3 hours)
- Wheel and Tire Package ($1,800, 2 hours)

## Configuration

The service runs on port 8085 by default and connects to a PostgreSQL database.

### Environment Variables
- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Hibernate DDL mode (validate for production)

## Running the Service

```powershell
# Build the service
.\mvnw.cmd clean package -pl services/modification-service -am -DskipTests

# Run database migrations
.\mvnw.cmd -pl services/modification-service flyway:migrate

# Start the service
.\mvnw.cmd -pl services/modification-service spring-boot:run
```

## Testing

The service includes comprehensive logging and validation:
- Input validation on all API endpoints
- Proper error handling and response codes
- Transaction management for request operations
- Status workflow management

## Business Logic

1. **Request Creation**: 
   - Validates service existence
   - Creates or retrieves customer record
   - Sets initial status to PENDING
   - Estimates cost based on service base price

2. **Request Update**:
   - Supports status changes with automatic timestamps
   - Tracks approval, rejection, and completion times
   - Allows admin notes and cost updates

3. **Status Workflow**:
   - PENDING → Initial state
   - APPROVED → Admin approved request
   - REJECTED → Admin rejected request
   - IN_PROGRESS → Work started
   - COMPLETED → Work finished
   - CANCELLED → Request cancelled
