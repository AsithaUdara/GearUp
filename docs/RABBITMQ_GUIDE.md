# RabbitMQ Guide — GearUp (Beginner Friendly)

This short guide explains how GearUp services communicate with RabbitMQ and shows simple, copy-paste examples for publishing and subscribing to events. It's written for beginners and assumes Java + Spring Boot microservices used in this repository.

**What you'll learn**

- How services publish events
- How services subscribe to events
- Where exchanges, queues and routing keys live in this repo
- A simple request-reply sketch (RPC) using RabbitMQ
- How to test locally and a checklist for production

---

## Quick architecture summary

- GearUp uses RabbitMQ topic exchanges for event-driven, publish/subscribe communication.
- Exchanges, queues and routing keys are centralized in `shared-libs/common-utils` so every service uses the same constants and event DTOs.
- Messages are JSON-serialized using Jackson (`Jackson2JsonMessageConverter`).
- Services publish events (fire-and-forget) with `RabbitTemplate.convertAndSend(...)` and consume using `@RabbitListener`.

Key files to inspect

- `shared-libs/common-utils/src/main/java/com/gearup/shared/messaging/RabbitMQConstants.java` — exchanges, queues, routing keys
- `shared-libs/common-utils/src/main/java/com/gearup/shared/config/SharedRabbitMQConfig.java` — auto-declared exchanges/queues, `RabbitTemplate`, `RabbitAdmin`
- `shared-libs/common-utils/src/main/java/com/gearup/shared/messaging/EventPublisher.java` — convenience publisher with optional Redis deduplication

---

## 1) Publish an event (what other services will see)

Use the shared constants and shared event DTOs. Example: the template service publishes `template.created`.

Example publisher (copy from `services/template-service/.../TemplateEventPublisher.java`):

```java
// imports omitted for brevity
public void publishCreated(ServiceTemplateDto template, String performedBy) {
    ServiceTemplateCreatedEvent event = new ServiceTemplateCreatedEvent(
            template.getId(), template.getName(), template.getDescription(),
            template.getPrice(), template.getDurationMinutes(), template.getActive(), performedBy, LocalDateTime.now()
    );
    rabbitTemplate.convertAndSend(
            RabbitMQConstants.TEMPLATE_EXCHANGE,
            RabbitMQConstants.TEMPLATE_CREATED_KEY,
            event
    );
}
```

Notes:

- `rabbitTemplate` uses a `Jackson2JsonMessageConverter`, so the event DTO must be on the classpath of both publisher and consumer.
- Publisher code in this repo sometimes wraps `convertAndSend` in a shared `EventPublisher` which also supports optional Redis deduplication.

---

## 2) Subscribe to an event (how to receive info)

Create a listener using `@RabbitListener`. Reuse the shared queue constant so the binding already exists.

Example listener (simple, beginner-friendly):

```java
package com.gearup.yourservice.messaging;

import com.gearup.shared.event.ServiceTemplateCreatedEvent;
import com.gearup.shared.messaging.RabbitMQConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TemplateEventListener {

    @RabbitListener(queues = RabbitMQConstants.TEMPLATE_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onTemplateCreated(@Payload ServiceTemplateCreatedEvent event) {
        // 1) Log for confirmation
        System.out.println("Received template.created id=" + event.getTemplateId() + ", name=" + event.getName());
        // 2) Do your work: update cache, precompute permissions, create read-model, etc.
    }
}
```

Checklist when writing a listener:

- Add the `shared-libs` module as a dependency so you have `RabbitMQConstants` and event DTOs.
- Use the same DTO class (shared-libs) to avoid deserialization errors.
- Make message handling idempotent (events may be redelivered).

---

## 3) Request-Reply (RPC) sketch — only if you need immediate answers

Prefer HTTP for synchronous queries. If you need RabbitMQ RPC, Spring AMQP supports `convertSendAndReceive`.

Requester (call and wait for response):

```java
Object reply = rabbitTemplate.convertSendAndReceive("some.exchange", "some.request", requestDto);
if (reply == null) {
    // timed out or no response
}
// cast to expected response type
```

Responder (returns a value from the listener):

```java
@RabbitListener(queues = "some.request.queue")
public SomeResponse handle(SomeRequest request) {
    // compute and return
    return new SomeResponse(...);
}
```

Notes:

- You must ensure request/reply queue/bindings are configured and both sides understand the request/response DTOs.
- Set `rabbitTemplate.setReplyTimeout(...)` if you need control over waiting time.
- This repo primarily uses fire-and-forget events; there are no established RPC contracts here.

---

## 4) Application properties (where to set RabbitMQ connection)

Set RabbitMQ connection details in each service's `application.yml` or `bootstrap.yml`:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
# optional: timeouts
#    listener:
#      simple:
#        acknowledge-mode: auto
```

Search your repo for `spring.rabbitmq` to see environment-specific values.

---

## 5) Test locally (quick start)

1. Start a RabbitMQ container with management UI (PowerShell):

```powershell
docker run -d --name gearup-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

2. Open the management UI in a browser: `http://localhost:15672` (default `guest`/`guest`).

3. Publish a test message from the UI:

   - Go to Exchanges → click exchange (e.g., `template.exchange`) → Publish message
   - Use routing key `template.created` and a JSON body that matches `ServiceTemplateCreatedEvent` fields.

4. Run the consumer service and watch logs for the listener message.

Useful commands inside the container:

```powershell
docker exec -it gearup-rabbitmq rabbitmqctl list_queues
docker exec -it gearup-rabbitmq rabbitmqctl list_exchanges
```

---

## 6) Troubleshooting common issues

- Deserialization errors: ensure the consumer has the same event DTO (class name and package) on its classpath (the `shared-libs` module).
- Missing queue/exchange: `SharedRabbitMQConfig` uses `RabbitAdmin` to auto-declare at startup; ensure `RabbitAdmin` runs and app connects.
- Messages requeued / redelivered: handler exceptions cause requeues. Make handlers idempotent and log exceptions.
- Message disappears: check the dead-letter exchange/queue (`dlx.exchange`, `dlx.queue`) and the service DLQs (e.g., `notification.dlq`).

---

## 7) Beginner checklist before committing a listener

- [ ] Add `shared-libs` dependency so you reuse constants and DTOs
- [ ] Use the correct `RabbitMQConstants` exchange & routing key
- [ ] Add `@RabbitListener` method with `@Payload` typed to the shared event class
- [ ] Make handler idempotent (safe to run multiple times)
- [ ] Run RabbitMQ locally and test with a manual publish
- [ ] Add logging to confirm receipt (use `log.info`)

---

## 8) Production recommendations (short)

- Use dedicated RabbitMQ users and vhosts per environment; never use `guest` in production.
- Configure TLS and network policies for RabbitMQ access.
- Use schema versioning for events and add a `version` field if the event shape changes.
- Monitor RabbitMQ (queues, consumers, DLQs) with Prometheus / management UI.
- Review retry policies and move poison messages to DLQ after N attempts.
