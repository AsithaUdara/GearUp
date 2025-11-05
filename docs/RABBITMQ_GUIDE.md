# RabbitMQ Setup and Usage Guide

## Quick Start

### 1. Start RabbitMQ with Docker

```bash
# Using docker-compose (recommended)
cd deployment/docker
docker-compose up -d rabbitmq

# Or standalone
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.13-management
```

### 2. Access RabbitMQ Management UI

```
URL: http://localhost:15672
Username: guest
Password: guest
```

### 3. Verify Connection

```bash
# Check if RabbitMQ is running
docker ps | grep rabbitmq

# Test connection from notification-service
curl http://localhost:8081/actuator/health
```

## RabbitMQ Concepts

### Exchanges

An **exchange** receives messages from producers and routes them to queues.

**Types:**
- **Direct:** Routes based on exact routing key match
- **Topic:** Routes based on pattern matching (we use this)
- **Fanout:** Routes to all bound queues
- **Headers:** Routes based on message headers

**GearUp Configuration:**
```java
@Bean
public TopicExchange notificationExchange() {
    return new TopicExchange("notification.exchange");
}
```

### Queues

A **queue** stores messages until they are consumed.

**GearUp Queues:**
- `notification.queue` - All notification events
- `notification.dlq` - Dead letter queue for failed messages

```java
@Bean
public Queue notificationQueue() {
    return QueueBuilder.durable("notification.queue")
            .withArgument("x-dead-letter-exchange", "notification.dlx")
            .build();
}
```

### Bindings

**Bindings** connect exchanges to queues with routing keys.

**GearUp Bindings:**
```java
@Bean
public Binding vehicleBookingCreatedBinding() {
    return BindingBuilder
            .bind(notificationQueue())
            .to(notificationExchange())
            .with("vehicle.booking.created");
}
```

### Routing Keys

**Routing keys** determine how messages are routed from exchanges to queues.

**GearUp Routing Key Pattern:**
```
{entity}.{action}

Examples:
- vehicle.booking.created
- vehicle.booking.confirmed
- invoice.created
- invoice.paid
- task.assigned
```

## Publishing Events

### Step 1: Create Event Model

```java
package com.gearup.shared.event;

public class VehicleBookingCreatedEvent extends BaseNotificationEvent {
    private String bookingId;
    private String vehicleId;
    private String vehicleName;
    private String customerName;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    private Double totalAmount;
    
    // Constructor, getters, setters...
}
```

### Step 2: Use EventPublisher

```java
@Service
public class BookingService {
    
    @Autowired
    private EventPublisher eventPublisher;
    
    public Booking createBooking(BookingRequest request) {
        // 1. Save booking to database
        Booking booking = bookingRepository.save(newBooking);
        
        // 2. Create event
        VehicleBookingCreatedEvent event = new VehicleBookingCreatedEvent(
            UUID.randomUUID().toString(),  // eventId
            request.getUserId(),            // userId
            LocalDateTime.now(),            // timestamp
            booking.getId(),                // bookingId
            booking.getVehicleId(),         // vehicleId
            booking.getVehicleName(),       // vehicleName
            booking.getCustomerName(),      // customerName
            booking.getStartDate(),         // bookingStartDate
            booking.getEndDate(),           // bookingEndDate
            booking.getTotalAmount()        // totalAmount
        );
        
        // 3. Publish event
        eventPublisher.publishVehicleBookingCreated(event);
        
        return booking;
    }
}
```

## Consuming Events

### Step 1: Create Event Listener

```java
@Component
public class NotificationEventListener {
    
    @Autowired
    private NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleVehicleBookingCreatedEvent(VehicleBookingCreatedEvent event) {
        try {
            log.info("Received VehicleBookingCreatedEvent: {}", event.getEventId());
            
            // Create notification for the user
            NotificationRequest request = NotificationRequest.builder()
                .userId(event.getUserId())
                .title("New Booking Created")
                .message(String.format(
                    "Your booking for %s has been created. Total: $%.2f",
                    event.getVehicleName(),
                    event.getTotalAmount()
                ))
                .type(NotificationType.BOOKING_CREATED)
                .priority(NotificationPriority.HIGH)
                .relatedEntityId(event.getBookingId())
                .relatedEntityType("BOOKING")
                .build();
            
            notificationService.createNotification(request);
            
            log.info("Successfully created notification for event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Error processing VehicleBookingCreatedEvent: {}", event.getEventId(), e);
            // Message will be sent to DLQ if configured
        }
    }
}
```

### Step 2: Configuration

```properties
# application.properties

# RabbitMQ Connection
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Listener Configuration
spring.rabbitmq.listener.simple.concurrency=5
spring.rabbitmq.listener.simple.max-concurrency=10
spring.rabbitmq.listener.simple.prefetch=10
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.initial-interval=1000
```

## Message Flow Example

```
┌──────────────────┐
│  Vehicle Service │
└────────┬─────────┘
         │
         │ 1. Publish Event
         │
         ▼
┌─────────────────────────────┐
│  RabbitMQ (Exchange)        │
│  notification.exchange      │
└────────┬────────────────────┘
         │
         │ 2. Route by Key
         │ "vehicle.booking.created"
         │
         ▼
┌─────────────────────────────┐
│  Queue                      │
│  notification.queue         │
└────────┬────────────────────┘
         │
         │ 3. Consume
         │
         ▼
┌─────────────────────────────┐
│  Notification Service       │
│  @RabbitListener            │
└─────────────────────────────┘
```

## Testing

### 1. Using Event Simulator

```bash
# Simulate vehicle booking created event
curl -X POST "http://localhost:8081/api/events/simulate/vehicle-booking-created?userId=test-user-123"

# Expected Response:
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Vehicle booking created event published successfully",
  "userId": "test-user-123"
}
```

### 2. Verify in RabbitMQ Management UI

1. Go to **Queues** tab
2. Click on `notification.queue`
3. Check **Message rates** - should show incoming/outgoing messages
4. Use **Get messages** to peek at message content

### 3. Check Notification Created

```bash
# Get notifications for user
curl "http://localhost:8081/api/notifications/user/test-user-123"

# Expected: New notification about vehicle booking
```

### 4. Monitor Logs

```bash
# Notification service logs
docker logs -f gearup-notification-service

# Look for:
# "Received VehicleBookingCreatedEvent: ..."
# "Successfully created notification for event: ..."
```

## Advanced Configuration

### Message TTL (Time To Live)

```java
@Bean
public Queue notificationQueue() {
    return QueueBuilder.durable("notification.queue")
            .ttl(3600000)  // Messages expire after 1 hour
            .build();
}
```

### Dead Letter Queue

```java
@Bean
public Queue notificationDeadLetterQueue() {
    return new Queue("notification.dlq", true);
}

@Bean
public TopicExchange deadLetterExchange() {
    return new TopicExchange("notification.dlx");
}

@Bean
public Queue notificationQueue() {
    return QueueBuilder.durable("notification.queue")
            .withArgument("x-dead-letter-exchange", "notification.dlx")
            .withArgument("x-dead-letter-routing-key", "notification.dead")
            .build();
}
```

### Priority Queues

```java
@Bean
public Queue priorityQueue() {
    return QueueBuilder.durable("notification.priority.queue")
            .maxPriority(10)  // 0-10, higher = more important
            .build();
}

// When publishing
rabbitTemplate.convertAndSend(
    exchange,
    routingKey,
    message,
    msg -> {
        msg.getMessageProperties().setPriority(9);  // High priority
        return msg;
    }
);
```

### Message Persistence

```java
// Messages survive RabbitMQ restart
@Bean
public Queue durableQueue() {
    return new Queue("notification.queue", true);  // durable = true
}

// Publishing with persistent delivery mode
rabbitTemplate.convertAndSend(
    exchange,
    routingKey,
    message,
    msg -> {
        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return msg;
    }
);
```

## Monitoring and Debugging

### Enable Debug Logging

```properties
logging.level.org.springframework.amqp=DEBUG
logging.level.org.springframework.amqp.rabbit=DEBUG
logging.level.com.gearup.notificationservice.listener=DEBUG
```

### RabbitMQ Management API

```bash
# List all queues
curl -u guest:guest http://localhost:15672/api/queues

# Get queue details
curl -u guest:guest http://localhost:15672/api/queues/%2F/notification.queue

# List exchanges
curl -u guest:guest http://localhost:15672/api/exchanges

# List bindings
curl -u guest:guest http://localhost:15672/api/bindings
```

### Health Checks

```bash
# Spring Boot Actuator
curl http://localhost:8081/actuator/health

# Should show RabbitMQ status:
{
  "status": "UP",
  "components": {
    "rabbit": {
      "status": "UP",
      "details": {
        "version": "3.13.0"
      }
    }
  }
}
```

## Common Issues and Solutions

### Issue 1: Connection Refused

**Symptoms:**
```
Connection refused: connect
```

**Solutions:**
1. Check if RabbitMQ is running: `docker ps | grep rabbitmq`
2. Verify port mapping: `-p 5672:5672`
3. Check firewall settings
4. Verify connection settings in `application.properties`

### Issue 2: Messages Not Being Consumed

**Symptoms:**
- Messages accumulate in queue
- Listener not triggered

**Solutions:**
1. Check listener is enabled: `@EnableRabbit` annotation
2. Verify queue name matches in listener
3. Check message format matches event class
4. Review error logs for deserialization issues

### Issue 3: Authentication Failed

**Symptoms:**
```
ACCESS_REFUSED - Login was refused
```

**Solutions:**
1. Verify username/password in properties
2. Check RabbitMQ user permissions
3. Create user if needed:
   ```bash
   docker exec rabbitmq rabbitmqctl add_user myuser mypassword
   docker exec rabbitmq rabbitmqctl set_user_tags myuser administrator
   docker exec rabbitmq rabbitmqctl set_permissions -p / myuser ".*" ".*" ".*"
   ```

### Issue 4: Message Serialization Error

**Symptoms:**
```
Could not convert message
```

**Solutions:**
1. Ensure event class has default constructor
2. Add Jackson annotations if needed
3. Check Jackson2JsonMessageConverter is configured
4. Verify event class is in correct package

## Performance Tuning

### 1. Consumer Concurrency

```properties
# Number of concurrent consumers
spring.rabbitmq.listener.simple.concurrency=5
spring.rabbitmq.listener.simple.max-concurrency=10
```

### 2. Prefetch Count

```properties
# Number of messages to prefetch
spring.rabbitmq.listener.simple.prefetch=10
```

Higher prefetch = better throughput, but more memory usage

### 3. Connection Pooling

```java
@Bean
public CachingConnectionFactory connectionFactory() {
    CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
    factory.setUsername("guest");
    factory.setPassword("guest");
    factory.setChannelCacheSize(25);
    return factory;
}
```

### 4. Batch Publishing

```java
// Publish multiple messages efficiently
rabbitTemplate.setChannelTransacted(true);
rabbitTemplate.execute(channel -> {
    for (Event event : events) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
    return null;
});
```

## Best Practices

### 1. Event Design
- ✅ Keep events immutable
- ✅ Include all necessary data (avoid additional queries)
- ✅ Use clear, descriptive names
- ✅ Version your events for backward compatibility

### 2. Error Handling
- ✅ Implement retry logic with exponential backoff
- ✅ Use dead letter queues for failed messages
- ✅ Log all errors with context
- ✅ Monitor DLQ for recurring issues

### 3. Idempotency
- ✅ Design consumers to handle duplicate messages
- ✅ Use event IDs to track processed events
- ✅ Store processed event IDs in database/cache

### 4. Monitoring
- ✅ Monitor queue depth (alert if growing)
- ✅ Track message processing time
- ✅ Set up alerts for DLQ messages
- ✅ Monitor connection count and memory usage

## Production Checklist

- [ ] RabbitMQ cluster setup (HA)
- [ ] Enable message persistence
- [ ] Configure dead letter queues
- [ ] Set up monitoring and alerts
- [ ] Implement retry policies
- [ ] Test failover scenarios
- [ ] Document event schemas
- [ ] Set resource limits (memory, connections)
- [ ] Enable TLS/SSL for production
- [ ] Configure authentication and authorization
- [ ] Back up RabbitMQ definitions
- [ ] Load test with expected traffic

## Additional Resources

- **RabbitMQ Official Docs:** https://www.rabbitmq.com/documentation.html
- **Spring AMQP Docs:** https://docs.spring.io/spring-amqp/reference/
- **Management Plugin:** https://www.rabbitmq.com/management.html
- **RabbitMQ Patterns:** https://www.rabbitmq.com/getstarted.html

---

**Need Help?** Check the logs, RabbitMQ management UI, and cross-service communication guide for troubleshooting.
