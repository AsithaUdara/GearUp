# Event-Driven Architecture Diagram - GearUp

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                           GearUp Event-Driven Architecture                              │
│                                  RabbitMQ + Redis                                        │
└─────────────────────────────────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                                EVENT PUBLISHERS                                          │
└──────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│  Appointment    │   │  Modification   │   │    Tracking     │   │    Payment      │
│    Service      │   │     Service     │   │    Service      │   │    Service      │
├─────────────────┤   ├─────────────────┤   ├─────────────────┤   ├─────────────────┤
│ • Created       │   │ • RequestCreated│   │ • TaskCreated   │   │ • InvoiceCreated│
│ • Confirmed     │   │ • Approved      │   │ • TaskStarted   │   │ • InvoicePaid   │
│ • Cancelled     │   │ • Rejected      │   │ • ProgressUpdt  │   │ • PayCompleted  │
│ • Updated       │   │ • StatusChanged │   │ • TaskCompleted │   │ • InvoiceUpdt   │
│ • Approved      │   │ • Completed     │   │ • TaskBlocked   │   └─────────────────┘
│ • EmpAssigned   │   │ • Cancelled     │   │ • PartsRequest  │
│ • TimeslotBlock │   └─────────────────┘   └─────────────────┘
└─────────────────┘                                                ┌─────────────────┐
                                                                   │   User-Auth     │
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐  │    Service      │
│    Vehicle      │   │     Parts       │   │    Customer     │  ├─────────────────┤
│    Service      │   │    Service      │   │    Service      │  │ • UserRegister  │
├─────────────────┤   ├─────────────────┤   ├─────────────────┤  │ • RoleAssigned  │
│ • VehicleReg    │   │ • StatusChanged │   │ • CustRegister  │  │ • UserUpdated   │
│ • VehicleUpdt   │   │ • InventoryLow  │   │ • CustUpdated   │  │ • UserDeactive  │
└─────────────────┘   └─────────────────┘   │ • KycChanged    │  │ • PwdChanged    │
                                             └─────────────────┘  └─────────────────┘
                                  │
                                  │
                                  ▼

┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                              RABBITMQ MESSAGE BROKER                                     │
│                                                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │
│  │ appointment. │  │modification. │  │  tracking.   │  │   payment.   │               │
│  │   exchange   │  │   exchange   │  │   exchange   │  │   exchange   │               │
│  │   (Topic)    │  │   (Topic)    │  │   (Topic)    │  │   (Topic)    │               │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘               │
│         │                 │                  │                 │                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │
│  │    user.     │  │  customer.   │  │  vehicle.    │  │   parts.     │               │
│  │   exchange   │  │   exchange   │  │   exchange   │  │   exchange   │               │
│  │   (Topic)    │  │   (Topic)    │  │   (Topic)    │  │   (Topic)    │               │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘               │
│         │                 │                  │                 │                        │
│         └─────────────────┴──────────────────┴─────────────────┘                        │
│                                     │                                                    │
│                    ┌────────────────┴────────────────┐                                  │
│                    │                                 │                                  │
│                    ▼                                 ▼                                  │
│         ┌──────────────────┐             ┌──────────────────┐                          │
│         │  notification.   │             │   analytics.     │                          │
│         │     queue        │             │     queue        │                          │
│         │                  │             │                  │                          │
│         │  Routing Keys:   │             │  Routing Keys:   │                          │
│         │  - specific keys │             │  - *.# (ALL)     │                          │
│         └────────┬─────────┘             └────────┬─────────┘                          │
│                  │                                │                                     │
└──────────────────┼────────────────────────────────┼─────────────────────────────────────┘
                   │                                │
                   │                                │
                   ▼                                ▼

┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                                EVENT CONSUMERS                                           │
└──────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐      ┌─────────────────────────────────────┐
│      Notification Service           │      │       Analytical Service            │
├─────────────────────────────────────┤      ├─────────────────────────────────────┤
│                                     │      │                                     │
│ ┌─────────────────────────────────┐ │      │ ┌─────────────────────────────────┐ │
│ │  AppointmentEventListener       │ │      │ │   AnalyticsEventListener        │ │
│ │  • appointmentCreated (6)       │ │      │ │   • handleAllEvents() - Generic │ │
│ │  • appointmentApproved          │ │      │ │                                 │ │
│ │  • appointmentCancelled         │ │      │ │   Listens to ALL events via:    │ │
│ │  • employeeAssigned             │ │      │ │   • appointment.#               │ │
│ │  • ... more handlers            │ │      │ │   • modification.#              │ │
│ └─────────────────────────────────┘ │      │ │   • tracking.#                  │ │
│                                     │      │ │   • payment.#                   │ │
│ ┌─────────────────────────────────┐ │      │ │   • user.#                      │ │
│ │  ModificationEventListener      │ │      │ │   • customer.#                  │ │
│ │  • modificationApproved (3)     │ │      │ │   • vehicle.#                   │ │
│ │  • modificationCompleted        │ │      │ │   • parts.#                     │ │
│ │  • ... more handlers            │ │      │ │                                 │ │
│ └─────────────────────────────────┘ │      │ │   Purpose:                      │ │
│                                     │      │ │   • Comprehensive analytics     │ │
│ ┌─────────────────────────────────┐ │      │ │   • Reporting & auditing        │ │
│ │  TrackingEventListener          │ │      │ │   • Metrics collection          │ │
│ │  • taskCompleted (3)            │ │      │ │   • Dashboard data              │ │
│ │  • issueReported                │ │      │ └─────────────────────────────────┘ │
│ │  • ... more handlers            │ │      │                                     │
│ └─────────────────────────────────┘ │      │  Coverage: 49/49 events (100%)     │
│                                     │      │                                     │
│ ┌─────────────────────────────────┐ │      └─────────────────────────────────────┘
│ │  CrossServiceEventListener      │ │
│ │  • Payment events (3)           │ │
│ │    - paymentCompleted           │ │
│ │    - invoiceCreated             │ │
│ │    - invoicePaid                │ │
│ │                                 │ │
│ │  • Customer events (2)          │ │
│ │    - customerRegistered         │ │
│ │    - customerKycChanged         │ │
│ │                                 │ │
│ │  • User events (2)              │ │
│ │    - userRegistered             │ │
│ │    - roleAssigned               │ │
│ │                                 │ │
│ │  • Vehicle events (2)           │ │
│ │    - vehicleRegistered          │ │
│ │    - vehicleUpdated             │ │
│ └─────────────────────────────────┘ │
│                                     │
│  Coverage: 22/49 events (45%)      │
│  Purpose: Cross-service comms      │
│                                     │
└─────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                              REDIS DEDUPLICATION                                         │
│                                                                                          │
│  Key Pattern: event:{eventId}                                                           │
│  TTL: 24 hours                                                                          │
│  Purpose: Prevent duplicate event processing                                            │
│                                                                                          │
│  All publishers check Redis before publishing to RabbitMQ                               │
└──────────────────────────────────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                            EVENT STATISTICS                                              │
├──────────────────────────────────────────────────────────────────────────────────────────┤
│  Total Events: 49 classes                                                               │
│  Total Exchanges: 11 (8 domain + 3 system)                                              │
│  Total Queues: 9                                                                        │
│  Total Routing Keys: 50+                                                                │
│  Event Publishers: 8 services                                                           │
│  Event Consumers: 2 services (notification, analytics)                                  │
│  Coverage: 100% (all events captured by analytics)                                      │
│  Build Status: ✅ SUCCESS (59.443 seconds)                                              │
└──────────────────────────────────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                         EXAMPLE EVENT FLOW                                               │
│                                                                                          │
│  1. Customer creates appointment                                                        │
│     ↓                                                                                    │
│  2. AppointmentService.createAppointment()                                              │
│     ↓                                                                                    │
│  3. Check Redis: event:{appointmentId} exists?                                          │
│     ├─ Yes → Skip (duplicate)                                                           │
│     └─ No  → Continue                                                                   │
│     ↓                                                                                    │
│  4. Set Redis key (TTL: 24h)                                                            │
│     ↓                                                                                    │
│  5. Publish to appointment.exchange (routing: appointment.created)                      │
│     ↓                                                                                    │
│  6. RabbitMQ routes to:                                                                 │
│     ├─ notification.queue → NotificationService                                         │
│     │   └─ Send confirmation email/SMS                                                  │
│     │                                                                                    │
│     └─ analytics.queue → AnalyticalService                                              │
│         └─ Store appointment metrics                                                    │
│                                                                                          │
└──────────────────────────────────────────────────────────────────────────────────────────┘
```
