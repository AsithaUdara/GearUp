# GearUp Microservices Communication Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          External Clients                                    │
│                     (Web, Mobile, Third-party)                              │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 │ HTTPS
                                 │
                    ┌────────────▼─────────────┐
                    │     API Gateway          │
                    │     (Port 8080)          │
                    │  ┌──────────────────┐    │
                    │  │ Rate Limiting    │    │
                    │  │ Authentication   │    │
                    │  │ Routing          │    │
                    │  │ Load Balancing   │    │
                    │  └──────────────────┘    │
                    └────────────┬─────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │   Eureka Server          │
                    │   Service Discovery      │
                    │   (Port 8761)            │
                    │                          │
                    │  Registered Services:    │
                    │  • notification-service  │
                    │  • user-auth-service     │
                    │  • vehicle-service       │
                    │  • billing-service       │
                    └──────────────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
┌───────▼───────┐       ┌────────▼────────┐     ┌────────▼────────┐
│ notification- │       │  user-auth-     │     │   vehicle-      │
│   service     │       │    service      │     │   service       │
│  (Port 8081)  │       │  (Port 8082)    │     │  (Port 8083)    │
│               │       │                 │     │                 │
│ ┌───────────┐ │       │ ┌─────────────┐ │     │ ┌─────────────┐ │
│ │Feign      │ │◄──────┤ │REST API     │ │     │ │Event        │ │
│ │Client     │ │       │ │Endpoints    │ │     │ │Publisher    │ │
│ └───────────┘ │       │ └─────────────┘ │     │ └─────────────┘ │
│               │       │                 │     │                 │
│ ┌───────────┐ │       │ ┌─────────────┐ │     │ ┌─────────────┐ │
│ │Event      │ │       │ │Firebase     │ │     │ │Business     │ │
│ │Listener   │ │       │ │Auth         │ │     │ │Logic        │ │
│ └───────────┘ │       │ └─────────────┘ │     │ └─────────────┘ │
│               │       │                 │     │                 │
└───────┬───────┘       └─────────────────┘     └────────┬────────┘
        │                                                 │
        │              ┌──────────────────┐              │
        └──────────────►   RabbitMQ       ◄──────────────┘
                       │  Message Broker  │
                       │  (Port 5672)     │
                       │                  │
                       │  Exchange:       │
                       │  notification.   │
                       │    exchange      │
                       │                  │
                       │  Queues:         │
                       │  • notification  │
                       │    .queue        │
                       │  • billing.queue │
                       │  • vehicle.queue │
                       └──────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
┌───────▼───────┐     ┌─────────▼────────┐   ┌─────────▼────────┐
│  PostgreSQL   │     │     Redis        │   │   Config Server  │
│  (Port 5432)  │     │   (Port 6379)    │   │   (Port 8888)    │
│               │     │                  │   │                  │
│ • Users DB    │     │ • Cache          │   │ • Centralized    │
│ • Notif. DB   │     │ • Sessions       │   │   Configuration  │
│ • Vehicle DB  │     │ • Rate Limiting  │   │ • Git-backed     │
│ • Billing DB  │     │ • Pub/Sub        │   │ • Environment    │
└───────────────┘     └──────────────────┘   │   Specific       │
                                              └──────────────────┘
```

## Communication Patterns

### 1. Synchronous Communication (Feign + Eureka)

```
┌─────────────────────┐
│ notification-service│
│                     │
│  Need user details? │
└──────────┬──────────┘
           │
           │ 1. Call getUserDetails(userId)
           │
     ┌─────▼──────┐
     │   Feign    │
     │   Client   │
     └─────┬──────┘
           │
           │ 2. Resolve service location
           │
     ┌─────▼──────┐
     │   Eureka   │
     │   Server   │
     └─────┬──────┘
           │
           │ 3. Return instance: user-auth-service:8082
           │
     ┌─────▼──────────┐
     │ user-auth-     │
     │   service      │
     │                │
     │ GET /api/users │
     │     /{userId}  │
     └─────┬──────────┘
           │
           │ 4. Return UserDetailsResponse
           │
     ┌─────▼──────────┐
     │ notification-  │
     │   service      │
     │                │
     │ Process with   │
     │ user data      │
     └────────────────┘
```

### 2. Asynchronous Communication (RabbitMQ)

```
┌─────────────────────┐
│  vehicle-service    │
│                     │
│ User books vehicle  │
└──────────┬──────────┘
           │
           │ 1. Save booking to DB
           │
           │ 2. Create VehicleBookingCreatedEvent
           │
     ┌─────▼──────┐
     │   Event    │
     │  Publisher │
     └─────┬──────┘
           │
           │ 3. Publish to RabbitMQ
           │    routing key: "vehicle.booking.created"
           │
     ┌─────▼─────────────┐
     │     RabbitMQ      │
     │  notification.    │
     │    exchange       │
     └─────┬─────────────┘
           │
           │ 4. Route to bound queues
           │
     ┌─────▼─────────────┐
     │ notification.queue│
     └─────┬─────────────┘
           │
           │ 5. Consume event
           │
┌──────────▼──────────────┐
│  notification-service   │
│                         │
│  @RabbitListener        │
│                         │
│  handleBookingCreated() │
└──────────┬──────────────┘
           │
           │ 6. Create notification
           │
     ┌─────▼──────┐
     │ PostgreSQL │
     │            │
     │ Save       │
     │ notification│
     └────────────┘
```

### 3. API Gateway Routing

```
┌─────────────────────┐
│   Mobile App        │
│                     │
│ POST /api/bookings  │
└──────────┬──────────┘
           │
           │ 1. HTTPS Request
           │
     ┌─────▼──────────┐
     │  API Gateway   │
     │  (Port 8080)   │
     │                │
     │  Validate JWT  │
     │  Rate Limit    │
     └─────┬──────────┘
           │
           │ 2. Query Eureka for "vehicle-service"
           │
     ┌─────▼──────┐
     │   Eureka   │
     │   Server   │
     └─────┬──────┘
           │
           │ 3. Return: http://vehicle-service:8083
           │
     ┌─────▼──────────┐
     │ vehicle-       │
     │   service      │
     │                │
     │ POST /bookings │
     └─────┬──────────┘
           │
           │ 4. Process & Respond
           │
     ┌─────▼──────────┐
     │  API Gateway   │
     │                │
     │  Add headers   │
     │  Return JSON   │
     └─────┬──────────┘
           │
           │ 5. Response to client
           │
┌──────────▼──────────┐
│   Mobile App        │
│                     │
│ Display success     │
└─────────────────────┘
```

## Event Flow Example: Complete Booking Process

```
┌────────────┐
│   User     │
│            │
│  Books     │
│  Vehicle   │
└─────┬──────┘
      │
      │ HTTP POST /api/bookings
      │
┌─────▼─────────────┐
│  API Gateway      │
│  Authenticate     │
│  Route to service │
└─────┬─────────────┘
      │
┌─────▼──────────────┐
│ vehicle-service    │
│                    │
│ 1. Validate data   │
│ 2. Save booking    │
│ 3. Publish event   │
└─────┬──────────────┘
      │
      ├──────────────────────────────┐
      │                              │
      │ RabbitMQ Event               │ DB Transaction
      │                              │
┌─────▼──────────────┐         ┌─────▼─────┐
│    RabbitMQ        │         │PostgreSQL │
│                    │         │           │
│ vehicle.booking.   │         │ bookings  │
│   created event    │         │   table   │
└─────┬──────────────┘         └───────────┘
      │
      ├────────────────┬────────────────┐
      │                │                │
┌─────▼──────────┐ ┌───▼───────────┐ ┌─▼──────────┐
│notification-   │ │ billing-      │ │ analytics- │
│  service       │ │   service     │ │  service   │
│                │ │               │ │            │
│ Create notify  │ │ Generate      │ │ Track      │
│ for customer   │ │   invoice     │ │  metrics   │
└─────┬──────────┘ └───┬───────────┘ └─┬──────────┘
      │                │                │
      │                │                │
┌─────▼──────────┐ ┌───▼───────────┐ ┌─▼──────────┐
│ PostgreSQL     │ │ PostgreSQL    │ │  Redis     │
│ (notifications)│ │ (invoices)    │ │ (metrics)  │
└─────┬──────────┘ └───┬───────────┘ └────────────┘
      │                │
      │                │
      └────────┬───────┘
               │
      ┌────────▼────────┐
      │    User gets    │
      │   notification  │
      │   and invoice   │
      └─────────────────┘
```

## Service Dependencies

```
┌──────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                       │
│                                                               │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌──────────┐      │
│  │Eureka   │  │RabbitMQ │  │PostgreSQL│  │  Redis   │      │
│  │ Server  │  │         │  │          │  │          │      │
│  └─────────┘  └─────────┘  └──────────┘  └──────────┘      │
└──────────────────────────────────────────────────────────────┘
                           │
                           │ depends on
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                      Gateway Layer                            │
│                                                               │
│  ┌─────────────────┐         ┌──────────────────┐           │
│  │  API Gateway    │────────►│  Config Server   │           │
│  │                 │         │                  │           │
│  └─────────────────┘         └──────────────────┘           │
└──────────────────────────────────────────────────────────────┘
                           │
                           │ routes to
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                    Business Services                          │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │notification- │  │ user-auth-   │  │  vehicle-    │       │
│  │  service     │◄─┤  service     │  │  service     │       │
│  │              │  │              │  │              │       │
│  │  Consumes    │  │  Provides    │  │  Publishes   │       │
│  │  events      │  │  user data   │  │  events      │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  billing-    │  │  template-   │  │  analytics-  │       │
│  │  service     │  │  service     │  │  service     │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└──────────────────────────────────────────────────────────────┘
                           │
                           │ uses
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                    Shared Libraries                           │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ security-lib │  │ event-models │  │common-utils  │       │
│  │              │  │              │  │              │       │
│  │ Firebase     │  │ Event DTOs   │  │ RabbitMQ     │       │
│  │ Auth         │  │              │  │ Config       │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

## Port Reference

| Service | Port | Protocol | Purpose |
|---------|------|----------|---------|
| API Gateway | 8080 | HTTP | External entry point |
| notification-service | 8081 | HTTP | Notification APIs |
| user-auth-service | 8082 | HTTP | Authentication |
| vehicle-service | 8083 | HTTP | Vehicle management |
| billing-service | 8084 | HTTP | Billing operations |
| Eureka Server | 8761 | HTTP | Service discovery |
| Config Server | 8888 | HTTP | Configuration |
| RabbitMQ | 5672 | AMQP | Message broker |
| RabbitMQ Management | 15672 | HTTP | Admin UI |
| PostgreSQL | 5432 | TCP | Database |
| Redis | 6379 | TCP | Cache/Session |

## Data Flow Patterns

### Pattern 1: Command (Synchronous)
```
Client → API Gateway → Service → Database → Response
```
**Use for:** CRUD operations, immediate responses needed

### Pattern 2: Event (Asynchronous)
```
Service A → RabbitMQ → Service B → Database
```
**Use for:** Notifications, background processing, decoupling

### Pattern 3: Query (Feign)
```
Service A → Eureka → Service B → Response
```
**Use for:** Fetching data from other services

### Pattern 4: Hybrid
```
Client → Gateway → Service A → Database
                         ↓
                    RabbitMQ
                         ↓
                    Service B → Database
```
**Use for:** Commands that trigger async workflows

---

**For detailed implementation guides, see:**
- [CROSS_SERVICE_COMMUNICATION.md](CROSS_SERVICE_COMMUNICATION.md)
- [RABBITMQ_GUIDE.md](RABBITMQ_GUIDE.md)
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
