package com.gearup.shared.testing;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for testing event-driven architecture
 * Provides helper methods for verifying event publishing and consumption
 */
@Component
public class EventTestingHelper {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public EventTestingHelper(RabbitTemplate rabbitTemplate, 
                             RedisTemplate<String, String> redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Wait for an event to be published to a specific queue
     *
     * @param queueName Queue to monitor
     * @param timeoutSeconds Maximum time to wait
     * @return The received message or null if timeout
     */
    public Message waitForMessage(String queueName, long timeoutSeconds) {
        rabbitTemplate.setReceiveTimeout(timeoutSeconds * 1000);
        return rabbitTemplate.receive(queueName);
    }

    /**
     * Wait for an event and convert it to the expected type
     *
     * @param queueName Queue to monitor
     * @param eventType Expected event class
     * @param timeoutSeconds Maximum time to wait
     * @param <T> Event type
     * @return The received event or null if timeout
     */
    public <T> T waitForEvent(String queueName, Class<T> eventType, long timeoutSeconds) {
        Message message = waitForMessage(queueName, timeoutSeconds);
        if (message == null) {
            return null;
        }
        Object converted = rabbitTemplate.getMessageConverter().fromMessage(message);
        if (eventType.isInstance(converted)) {
            return eventType.cast(converted);
        }
        return null;
    }

    /**
     * Wait for an event using a custom listener
     *
     * @param exchange Exchange name
     * @param routingKey Routing key to bind to
     * @param eventType Expected event class
     * @param timeoutSeconds Maximum time to wait
     * @param <T> Event type
     * @return The received event or null if timeout
     */
    public <T> T waitForEventWithCustomListener(String exchange, 
                                                String routingKey, 
                                                Class<T> eventType, 
                                                long timeoutSeconds) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> receivedEvent = new AtomicReference<>();

        try {
            rabbitTemplate.execute(channel -> {
                // Create temporary queue
                String tempQueue = "test-queue-" + System.currentTimeMillis();
                channel.queueDeclare(tempQueue, false, true, true, null);
                channel.queueBind(tempQueue, exchange, routingKey);

                // Consume message
                channel.basicConsume(tempQueue, true, (consumerTag, delivery) -> {
                    try {
                        Object message = rabbitTemplate.getMessageConverter().fromMessage(
                                new Message(delivery.getBody(), delivery.getProperties())
                        );
                        if (eventType.isInstance(message)) {
                            receivedEvent.set(eventType.cast(message));
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }, consumerTag -> {});

                return null;
            });

            // Wait for event
            boolean received = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            return received ? receivedEvent.get() : null;

        } catch (Exception e) {
            System.err.println("Error setting up listener: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verify that an event ID has been processed and stored in Redis
     *
     * @param eventId Event ID to check
     * @return true if event was processed
     */
    public boolean isEventProcessed(String eventId) {
        String key = "event:processed:" + eventId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Get the remaining TTL for a processed event in Redis
     *
     * @param eventId Event ID to check
     * @return Remaining TTL in seconds, or -1 if not exists
     */
    public long getEventTTL(String eventId) {
        String key = "event:processed:" + eventId;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }

    /**
     * Clear all processed event markers from Redis
     */
    public void clearProcessedEvents() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .flushDb();
    }

    /**
     * Purge all messages from a queue
     *
     * @param queueName Queue to purge
     * @return Number of messages purged
     */
    public int purgeQueue(String queueName) {
        try {
            return rabbitTemplate.execute(channel -> {
                com.rabbitmq.client.AMQP.Queue.PurgeOk result = channel.queuePurge(queueName);
                return result.getMessageCount();
            });
        } catch (Exception e) {
            System.err.println("Error purging queue: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get the number of messages in a queue
     *
     * @param queueName Queue to check
     * @return Number of messages in queue
     */
    public long getQueueMessageCount(String queueName) {
        try {
            return rabbitTemplate.execute(channel -> {
                com.rabbitmq.client.AMQP.Queue.DeclareOk result = 
                    channel.queueDeclarePassive(queueName);
                return (long) result.getMessageCount();
            });
        } catch (Exception e) {
            System.err.println("Error getting queue count: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Verify that no event is received within the timeout period
     *
     * @param queueName Queue to monitor
     * @param waitSeconds Time to wait for unexpected events
     * @return true if no event received
     */
    public boolean verifyNoEvent(String queueName, long waitSeconds) {
        Message message = waitForMessage(queueName, waitSeconds);
        return message == null;
    }

    /**
     * Publish an event directly to an exchange for testing
     *
     * @param exchange Exchange name
     * @param routingKey Routing key
     * @param event Event object to publish
     */
    public void publishTestEvent(String exchange, String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    /**
     * Wait for Redis key to be set
     *
     * @param key Redis key to check
     * @param timeoutSeconds Maximum time to wait
     * @return true if key exists before timeout
     */
    public boolean waitForRedisKey(String key, long timeoutSeconds) {
        long endTime = System.currentTimeMillis() + (timeoutSeconds * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                return true;
            }
            try {
                Thread.sleep(100); // Check every 100ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Create a test queue and bind it to an exchange
     *
     * @param exchange Exchange name
     * @param routingKey Routing key to bind
     * @return Name of the created queue
     */
    public String createTestQueue(String exchange, String routingKey) {
        String queueName = "test-queue-" + System.currentTimeMillis();
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDeclare(queueName, false, true, true, null);
                channel.queueBind(queueName, exchange, routingKey);
                return null;
            });
            return queueName;
        } catch (Exception e) {
            System.err.println("Error creating test queue: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a test queue
     *
     * @param queueName Queue to delete
     */
    public void deleteTestQueue(String queueName) {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDelete(queueName);
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error deleting test queue: " + e.getMessage());
        }
    }
}
