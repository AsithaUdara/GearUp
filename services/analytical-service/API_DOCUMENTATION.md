# Analytical Service API Documentation

## Overview
The Analytical Service provides business intelligence and analytics data for the GearUp application, including dashboard metrics, service trends, top services, and recent activities.

## Base URL
```
http://localhost:8087
```

## Endpoints

### 1. Health Check
**GET** `/api/analytics/health`

Returns service health status.

**Response:**
```
Analytics Service is running!
```

---

### 2. Complete Dashboard
**GET** `/api/analytics/dashboard`

Returns all dashboard data in a single response (metrics, trends, top services, recent activities).

**Response:**
```json
{
  "metrics": {
    "appointments": 200,
    "appointmentsChange": "+7.0%",
    "newCustomers": 97,
    "newCustomersChange": "+5.4%",
    "growth": "12.4%"
  },
  "appointmentTrend": [...],
  "topServices": [...],
  "recentActivities": [...]
}
```

---

### 3. Dashboard Metrics
**GET** `/api/analytics/metrics`

Returns key business metrics.

**Response:**
```json
{
  "appointments": 342,
  "appointmentsChange": "+3.1%",
  "newCustomers": 347,
  "newCustomersChange": "+5.5%",
  "growth": "12.4%"
}
```

---

### 4. Appointment Trend
**GET** `/api/analytics/appointments/trend?weeks=12`

Returns appointment trends for the specified number of weeks.

**Query Parameters:**
- `weeks` (optional, default: 12) - Number of weeks to include in trend

**Response:**
```json
[
  {
    "date": "2025-08-14",
    "count": 0,
    "period": "Week 1"
  },
  {
    "date": "2025-08-21",
    "count": 85,
    "period": "Week 2"
  }
]
```

---

### 5. Top Services
**GET** `/api/analytics/services/top?limit=5`

Returns the most popular services.

**Query Parameters:**
- `limit` (optional, default: 5) - Number of top services to return

**Response:**
```json
[
  {
    "serviceName": "Oil Change",
    "count": 126,
    "percentage": 36.8
  },
  {
    "serviceName": "Brake Service",
    "count": 94,
    "percentage": 27.5
  }
]
```

---

### 6. Recent Activities
**GET** `/api/analytics/activities/recent?limit=10`

Returns recent system activities and events.

**Query Parameters:**
- `limit` (optional, default: 10) - Number of activities to return

**Response:**
```json
[
  {
    "timestamp": "2025-11-06T12:09:17.042648",
    "event": "Appointment confirmed: BMW 3 Series",
    "status": "OK",
    "timeAgo": "2m ago"
  },
  {
    "timestamp": "2025-11-06T11:56:17.042648",
    "event": "Inventory low: Oil Filter (OF-67890)",
    "status": "ATTENTION",
    "timeAgo": "15m ago"
  }
]
```

---

## Activity Status Types
- `OK` - Normal operation
- `ATTENTION` - Requires attention
- `WARNING` - Critical issue

---

## Testing with cURL

### Get Dashboard
```bash
curl http://localhost:8087/api/analytics/dashboard
```

### Get Top Services
```bash
curl http://localhost:8087/api/analytics/services/top?limit=5
```

### Get Recent Activities
```bash
curl http://localhost:8087/api/analytics/activities/recent?limit=10
```

---

## Frontend Integration Example

```typescript
// Fetch dashboard data
const response = await fetch('http://localhost:8087/api/analytics/dashboard');
const data = await response.json();

// Use in your component
const { metrics, appointmentTrend, topServices, recentActivities } = data;
```

---

## Mock Data

The service is pre-loaded with realistic mock data matching your UI design:

- **Appointments**: 342 (+3.1%)
- **New Customers**: 97 (+5.5%)
- **Growth**: 12.4%
- **Top Services**:
  1. Oil Change (126 bookings)
  2. Brake Service (94 bookings)
  3. Tire Rotation (83 bookings)
  4. Battery Check (57 bookings)
  5. Diagnostics (41 bookings)

- **12 Weeks of Appointment Trend Data** (showing gradual growth)
- **Recent Activities** with timestamps and status indicators

---

## Database Schema

The service uses the following tables:
- `service_analytics` - Service performance data
- `customer_analytics` - Customer growth metrics
- `popular_services` - Most booked services
- `recent_activity` - System event log
- `revenue_analytics` - Financial metrics
- `employee_performance` - Staff KPIs

All tables are created and managed by Flyway migrations.

---

## Notes

- All timestamps are in UTC
- CORS is enabled for all origins (development mode)
- The service automatically seeds mock data on first startup
- Data persists in PostgreSQL database `as_analytical_service`
