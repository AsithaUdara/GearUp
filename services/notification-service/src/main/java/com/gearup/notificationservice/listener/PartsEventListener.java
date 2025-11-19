package com.gearup.notificationservice.listener;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.parts.PartsInventoryLowEvent;
import com.gearup.shared.event.parts.PartsStatusChangedEvent;
import com.gearup.shared.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for parts-related events and sends notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PartsEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handlePartsInventoryLow(PartsInventoryLowEvent event) {
        try {
            log.info("🔔 Received PartsInventoryLowEvent: partId={}, partName={}, current={}, minimum={}",
                    event.getPartId(), event.getPartName(), event.getCurrentQuantity(), event.getMinimumQuantity());

            NotificationRequest notification = new NotificationRequest();
            notification.setUserId("PARTS_MANAGER"); // Send to parts manager
            notification.setTitle("⚠️ Low Inventory Alert");
            notification.setMessage(String.format("Part '%s' (ID: %d) is running low! Current stock: %d units (Minimum: %d). Supplier: %s. Please reorder immediately.",
                    event.getPartName(), event.getPartId(), event.getCurrentQuantity(), event.getMinimumQuantity(), event.getSupplier()));
            notification.setType(NotificationType.SYSTEM_ALERT);
            notification.setPriority(NotificationPriority.HIGH);
            notification.setRelatedEntityId(event.getPartId().toString());
            notification.setRelatedEntityType("PARTS");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");

            notificationService.createNotification(notification);
            log.info("✅ Sent low inventory alert for part: {}", event.getPartName());

        } catch (Exception e) {
            log.error("❌ Failed to send low inventory notification for part: {}", event.getPartName(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handlePartsStatusChanged(PartsStatusChangedEvent event) {
        try {
            log.info("🔔 Received PartsStatusChangedEvent: partId={}, partName={}, {} -> {}",
                    event.getPartId(), event.getPartName(), event.getOldStatus(), event.getNewStatus());

            // Determine notification details based on new status
            String title = String.format("Parts Status Updated: %s", event.getPartName());
            String message = String.format("Part '%s' (ID: %d) status changed from %s to %s by %s.",
                    event.getPartName(), event.getPartId(), event.getOldStatus(), event.getNewStatus(), event.getChangedBy());
            
            NotificationPriority priority = determinePriority(event.getNewStatus());
            String channels = determineChannels(event.getNewStatus());
            NotificationType type = determineType(event.getNewStatus());

            NotificationRequest notification = new NotificationRequest();
            notification.setUserId("PARTS_MANAGER"); // Send to parts manager
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);
            notification.setPriority(priority);
            notification.setRelatedEntityId(event.getPartId().toString());
            notification.setRelatedEntityType("PARTS");
            notification.setDeliveryChannels(channels);

            notificationService.createNotification(notification);
            log.info("✅ Sent parts status change notification for part: {}", event.getPartName());

        } catch (Exception e) {
            log.error("❌ Failed to send parts status notification for part: {}", event.getPartName(), e);
        }
    }

    /**
     * Determine notification priority based on parts status
     */
    private NotificationPriority determinePriority(String status) {
        return switch (status.toUpperCase()) {
            case "OUT_OF_STOCK", "DISCONTINUED" -> NotificationPriority.HIGH;
            case "ORDERED", "IN_TRANSIT" -> NotificationPriority.MEDIUM;
            default -> NotificationPriority.LOW;
        };
    }

    /**
     * Determine notification type based on parts status
     */
    private NotificationType determineType(String status) {
        return switch (status.toUpperCase()) {
            case "OUT_OF_STOCK", "DISCONTINUED" -> NotificationType.SYSTEM_ALERT;
            default -> NotificationType.GENERAL;
        };
    }

    /**
     * Determine notification channels based on parts status
     */
    private String determineChannels(String status) {
        return switch (status.toUpperCase()) {
            case "OUT_OF_STOCK", "DISCONTINUED" -> "[\"WEB\", \"EMAIL\", \"SMS\"]";
            case "ORDERED", "IN_TRANSIT", "AVAILABLE" -> "[\"WEB\", \"EMAIL\"]";
            default -> "[\"WEB\"]";
        };
    }
}
