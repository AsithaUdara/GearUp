package com.gearup.shared.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Generic Event Publisher for all microservices
 * Handles publishing events to RabbitMQ with Redis-based deduplication
 * 
 * Usage:
 * 1. Inject this component into your service
 * 2. Call publish() with exchange, routing key, and event object
 * 3. Events are automatically deduplicated using Redis
 */
@Component
public class EventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private static final String EVENT_DEDUP_KEY_PREFIX = "event:dedup:";
    private static final Duration DEDUP_TTL = Duration.ofHours(24);
    
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    public EventPublisher(RabbitTemplate rabbitTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Publish an event to RabbitMQ with deduplication
     * 
     * @param exchange The exchange to publish to
     * @param routingKey The routing key for the event
     * @param event The event object to publish
     * @param <T> The event type
     * @return true if published successfully, false if duplicate or failed
     */
    public <T> boolean publish(String exchange, String routingKey, T event) {
        return publish(exchange, routingKey, event, UUID.randomUUID().toString());
    }
    
    /**
     * Publish an event with a specific event ID for deduplication
     * 
     * @param exchange The exchange to publish to
     * @param routingKey The routing key for the event
     * @param event The event object to publish
     * @param eventId Unique event ID for deduplication
     * @param <T> The event type
     * @return true if published successfully, false if duplicate or failed
     */
    public <T> boolean publish(String exchange, String routingKey, T event, String eventId) {
        try {
            // Check for duplicate event
            if (isDuplicate(eventId)) {
                log.warn("Duplicate event detected: {} - skipping publication", eventId);
                return false;
            }
            
            // Publish to RabbitMQ
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            
            // Mark as published in Redis
            markAsPublished(eventId);
            
            log.info("✅ Event published: exchange={}, routingKey={}, eventId={}, eventType={}", 
                    exchange, routingKey, eventId, event.getClass().getSimpleName());
            
            return true;
            
        } catch (AmqpException e) {
            log.error("❌ Failed to publish event to RabbitMQ: exchange={}, routingKey={}, eventId={}", 
                    exchange, routingKey, eventId, e);
            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected error publishing event: eventId={}", eventId, e);
            return false;
        }
    }
    
    /**
     * Publish an event without deduplication (use with caution)
     * 
     * @param exchange The exchange to publish to
     * @param routingKey The routing key for the event
     * @param event The event object to publish
     * @param <T> The event type
     */
    public <T> void publishWithoutDeduplication(String exchange, String routingKey, T event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("✅ Event published (no dedup): exchange={}, routingKey={}, eventType={}", 
                    exchange, routingKey, event.getClass().getSimpleName());
        } catch (AmqpException e) {
            log.error("❌ Failed to publish event to RabbitMQ: exchange={}, routingKey={}", 
                    exchange, routingKey, e);
        }
    }
    
    /**
     * Check if an event has already been published
     */
    private boolean isDuplicate(String eventId) {
        try {
            String key = EVENT_DEDUP_KEY_PREFIX + eventId;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Redis check failed for event {}, allowing publication", eventId, e);
            return false; // If Redis fails, allow publication
        }
    }
    
    /**
     * Mark an event as published in Redis
     */
    private void markAsPublished(String eventId) {
        try {
            String key = EVENT_DEDUP_KEY_PREFIX + eventId;
            redisTemplate.opsForValue().set(key, "published", DEDUP_TTL);
        } catch (Exception e) {
            log.warn("Failed to mark event as published in Redis: {}", eventId, e);
            // Continue anyway - this is not critical
        }
    }
    
    /**
     * Clear deduplication cache for an event (admin/testing use)
     */
    public void clearDeduplication(String eventId) {
        try {
            String key = EVENT_DEDUP_KEY_PREFIX + eventId;
            redisTemplate.delete(key);
            log.info("Cleared deduplication for event: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to clear deduplication for event: {}", eventId, e);
        }
    }
}
