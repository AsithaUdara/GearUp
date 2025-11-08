package com.gearup.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.gearup.events.PartsRequestEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event Publisher Service for Parts Service
 * Publishes parts request events to RabbitMQ for inter-service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    
    private static final String PARTS_EXCHANGE = "parts.exchange";
    private static final String PARTS_REQUEST_CREATED_KEY = "parts.request.created";
    private static final String PARTS_REQUEST_UPDATED_KEY = "parts.request.updated";

    public void publishPartsRequestEvent(PartsRequestEvent event) {
        try {
            String routingKey = determineRoutingKey(event.getEventType());
            rabbitTemplate.convertAndSend(PARTS_EXCHANGE, routingKey, event);
            log.info("Published parts request event: {} with routing key: {}", event.getEventType(), routingKey);
        } catch (AmqpException e) {
            log.error("Failed to publish parts request event: {}", event.getEventType(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
    
    private String determineRoutingKey(String eventType) {
        return switch (eventType) {
            case "CREATED" -> PARTS_REQUEST_CREATED_KEY;
            case "UPDATED", "APPROVED", "REJECTED" -> PARTS_REQUEST_UPDATED_KEY;
            default -> PARTS_REQUEST_UPDATED_KEY;
        };
    }
}
