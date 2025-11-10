package com.gearup.service;

import com.gearup.shared.event.parts.PartsInventoryLowEvent;
import com.gearup.shared.event.parts.PartsStatusChangedEvent;
import com.gearup.shared.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event Publisher Service for Parts Service
 * Publishes parts-related events to RabbitMQ for inter-service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartsEventPublisher {

    private final com.gearup.shared.messaging.EventPublisher eventPublisher;

    /**
     * Publish event when parts inventory is low
     */
    public void publishPartsInventoryLowEvent(Long partId, String partName, int currentQuantity, int minimumQuantity, String supplier) {
        try {
            String eventId = UUID.randomUUID().toString();
            PartsInventoryLowEvent event = new PartsInventoryLowEvent(
                eventId,
                partId,
                partName,
                currentQuantity,
                minimumQuantity,
                supplier,
                LocalDateTime.now()
            );
            
            eventPublisher.publish(
                RabbitMQConstants.PARTS_EXCHANGE,
                RabbitMQConstants.PARTS_INVENTORY_LOW_KEY,
                event,
                eventId
            );
            
            log.info("📤 Published PartsInventoryLowEvent for part: {} (current: {}, minimum: {})", 
                partName, currentQuantity, minimumQuantity);
        } catch (Exception e) {
            log.error("❌ Failed to publish PartsInventoryLowEvent for part: {}", partName, e);
        }
    }

    /**
     * Publish event when parts status changes
     */
    public void publishPartsStatusChangedEvent(Long partId, String partName, String oldStatus, String newStatus, String changedBy) {
        try {
            String eventId = UUID.randomUUID().toString();
            PartsStatusChangedEvent event = new PartsStatusChangedEvent(
                eventId,
                partId,
                partName,
                oldStatus,
                newStatus,
                changedBy,
                LocalDateTime.now()
            );
            
            eventPublisher.publish(
                RabbitMQConstants.PARTS_EXCHANGE,
                RabbitMQConstants.PARTS_STATUS_CHANGED_KEY,
                event,
                eventId
            );
            
            log.info("📤 Published PartsStatusChangedEvent for part: {} ({} -> {})", 
                partName, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("❌ Failed to publish PartsStatusChangedEvent for part: {}", partName, e);
        }
    }
}
