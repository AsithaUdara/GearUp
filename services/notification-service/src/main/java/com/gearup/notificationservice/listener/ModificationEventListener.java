package com.gearup.notificationservice.listener;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.modification.*;
import com.gearup.shared.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for modification-related events and sends notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModificationEventListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleModificationRequestCreated(ModificationRequestCreatedEvent event) {
        try {
            log.info("🔔 Received ModificationRequestCreatedEvent: requestId={}", event.getModificationId());
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Modification Request Received");
            notification.setMessage(String.format("Your modification request for %s has been received. Request ID: #%d. Estimated cost: $%.2f",
                    event.getServiceName(), event.getModificationId(), event.getEstimatedCost()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.MEDIUM);
            notification.setRelatedEntityId(event.getModificationId().toString());
            notification.setRelatedEntityType("MODIFICATION_REQUEST");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
            
            notificationService.createNotification(notification);
            log.info("✅ Sent modification request created notification to customer: {}", event.getCustomerName());
            
        } catch (Exception e) {
            log.error("❌ Failed to send modification request created notification for request: {}", 
                event.getModificationId(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleModificationApproved(ModificationApprovedEvent event) {
        try {
            log.info("🔔 Received ModificationApprovedEvent: requestId={}", event.getModificationId());
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Modification Request Approved");
            notification.setMessage(String.format("Great news! Your %s modification request (ID: #%d) has been approved. We'll contact you shortly to schedule the work.",
                    event.getModificationType(), event.getModificationId()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.HIGH);
            notification.setRelatedEntityId(event.getModificationId().toString());
            notification.setRelatedEntityType("MODIFICATION_REQUEST");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");
            
            notificationService.createNotification(notification);
            log.info("✅ Sent modification approved notification to customer: {}", event.getCustomerName());
            
        } catch (Exception e) {
            log.error("❌ Failed to send modification approved notification for request: {}", 
                event.getModificationId(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleModificationRejected(ModificationRejectedEvent event) {
        try {
            log.info("🔔 Received ModificationRejectedEvent: requestId={}", event.getModificationId());
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Modification Request Update");
            notification.setMessage(String.format("Your %s modification request (ID: #%d) could not be approved at this time. Reason: %s. Please contact us for more information.",
                    event.getModificationType(), event.getModificationId(), event.getRejectionReason()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.HIGH);
            notification.setRelatedEntityId(event.getModificationId().toString());
            notification.setRelatedEntityType("MODIFICATION_REQUEST");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
            
            notificationService.createNotification(notification);
            log.info("✅ Sent modification rejected notification to customer: {}", event.getCustomerName());
            
        } catch (Exception e) {
            log.error("❌ Failed to send modification rejected notification for request: {}", 
                event.getModificationId(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleModificationCompleted(ModificationCompletedEvent event) {
        try {
            log.info("🔔 Received ModificationCompletedEvent: requestId={}", event.getModificationId());
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Modification Completed");
            notification.setMessage(String.format("Your %s modification has been completed! Total cost: $%.2f. Thank you for choosing our services.",
                    event.getModificationType(), event.getFinalCost()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.HIGH);
            notification.setRelatedEntityId(event.getModificationId().toString());
            notification.setRelatedEntityType("MODIFICATION_REQUEST");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");
            
            notificationService.createNotification(notification);
            log.info("✅ Sent modification completed notification to customer: {}", event.getCustomerName());
            
        } catch (Exception e) {
            log.error("❌ Failed to send modification completed notification for request: {}", 
                event.getModificationId(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleModificationCancelled(ModificationCancelledEvent event) {
        try {
            log.info("🔔 Received ModificationCancelledEvent: requestId={}", event.getModificationId());
            
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Modification Request Cancelled");
            notification.setMessage(String.format("Your %s modification request (ID: #%d) has been cancelled. Reason: %s",
                    event.getModificationType(), event.getModificationId(), event.getCancellationReason()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.MEDIUM);
            notification.setRelatedEntityId(event.getModificationId().toString());
            notification.setRelatedEntityType("MODIFICATION_REQUEST");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
            
            notificationService.createNotification(notification);
            log.info("✅ Sent modification cancelled notification to customer: {}", event.getCustomerName());
            
        } catch (Exception e) {
            log.error("❌ Failed to send modification cancelled notification for request: {}", 
                event.getModificationId(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleModificationStatusChanged(ModificationRequestStatusChangedEvent event) {
        try {
            log.info("🔔 Received ModificationRequestStatusChangedEvent: requestId={}, status: {} -> {}", 
                event.getModificationId(), event.getOldStatus(), event.getNewStatus());
            
            // Only send notification for status changes that aren't covered by other specific events
            if (!"APPROVED".equals(event.getNewStatus()) && 
                !"REJECTED".equals(event.getNewStatus()) && 
                !"COMPLETED".equals(event.getNewStatus()) &&
                !"CANCELLED".equals(event.getNewStatus())) {
                
                NotificationRequest notification = new NotificationRequest();
                notification.setUserId(event.getCustomerId());
                notification.setTitle("Modification Request Status Update");
                notification.setMessage(String.format("The status of your %s modification request (ID: #%d) has been updated to: %s",
                        event.getServiceName(), event.getModificationId(), event.getNewStatus()));
                notification.setType(NotificationType.GENERAL);
                notification.setPriority(NotificationPriority.LOW);
                notification.setRelatedEntityId(event.getModificationId().toString());
                notification.setRelatedEntityType("MODIFICATION_REQUEST");
                notification.setDeliveryChannels("[\"WEB\"]");
                
                notificationService.createNotification(notification);
                log.info("✅ Sent modification status changed notification to customer: {}", event.getCustomerName());
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to send modification status changed notification for request: {}", 
                event.getModificationId(), e);
        }
    }
}
