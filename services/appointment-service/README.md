# Appointment Service

The Appointment Service handles all appointment booking functionality for the GearUp platform. It provides REST APIs for managing services, time slots, and bookings.

## Features

- **Service Management**: List available services for booking
- **Time Slot Management**: Manage available time slots for each service
- **Booking Management**: Create, retrieve, update, and cancel bookings
- **Automatic Slot Management**: Time slots are automatically marked as unavailable when booked

## API Endpoints

### Services
- `GET /api/services` - Fetch list of available services
- `GET /api/services/{id}` - Fetch service by ID

### Time Slots
- `GET /api/timeslots?serviceId={serviceId}&date={date}` - Fetch available time slots for a given date/service
- `GET /api/timeslots/{id}` - Fetch time slot by ID

### Bookings
- `POST /api/bookings` - Create a new booking
- `GET /api/bookings?userId={userId}` - Retrieve user bookings
- `GET /api/bookings/{id}` - Get booking by ID
- `PUT /api/bookings/{id}` - Update booking
- `DELETE /api/bookings/{id}` - Cancel booking

## Database Schema

### Tables
- **services**: Available services for appointment booking
- **time_slots**: Available time slots for each service
- **bookings**: Customer appointment bookings

### Sample Services
The service comes pre-populated with sample services:
- Oil Change (30 minutes, $29.99)
- Tire Rotation (45 minutes, $39.99)
- Brake Inspection (60 minutes, $49.99)
- Battery Check (30 minutes, $19.99)
- General Inspection (90 minutes, $79.99)

## Configuration

The service runs on port 8084 by default and connects to a PostgreSQL database.

### Environment Variables
- `SPRING_DATASOURCE_URL`: Database URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Hibernate DDL mode (validate for production)

## Running the Service

```bash
# Build the service
mvn clean compile

# Run database migrations
mvn flyway:migrate

# Start the service
mvn spring-boot:run
```

## Testing

The service includes comprehensive logging and validation:
- Input validation on all API endpoints
- Proper error handling and response codes
- Transaction management for booking operations
- Automatic time slot availability management

## Business Logic

1. **Booking Creation**: 
   - Validates service and time slot existence
   - Checks time slot availability
   - Prevents double booking
   - Marks time slot as unavailable when booked

2. **Booking Cancellation**:
   - Updates booking status to CANCELLED
   - Makes time slot available again

3. **Time Slot Management**:
   - Automatically generates time slots for 30 days
   - Excludes weekends
   - Business hours: 9 AM - 5 PM
   - Hourly intervals with service duration consideration