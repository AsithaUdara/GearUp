package com.gearup.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.InvoiceCreatedEvent;
import com.gearup.shared.event.InvoicePaidEvent;
import com.gearup.shared.event.InvoiceUpdatedEvent;
import com.gearup.shared.event.TaskAssignedEvent;
import com.gearup.shared.event.TaskCompletedEvent;
import com.gearup.shared.event.VehicleBookingConfirmedEvent;
import com.gearup.shared.event.VehicleBookingCreatedEvent;
import com.gearup.shared.event.VehicleMaintenanceScheduledEvent;
import com.gearup.shared.messaging.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Listen to invoice created events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleInvoiceCreatedEvent(InvoiceCreatedEvent event) {
        log.info("Received InvoiceCreatedEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("New Invoice Created")
                    .message(String.format("Invoice %s has been created for %s with amount $%.2f", 
                            event.getInvoiceNumber(), event.getCustomerName(), event.getAmount()))
                    .type(NotificationType.INVOICE_CREATED)
                    .priority(NotificationPriority.MEDIUM)
                    .relatedEntityId(event.getInvoiceId())
                    .relatedEntityType("INVOICE")
                    .actionUrl("/invoices/" + event.getInvoiceId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for InvoiceCreatedEvent");
        } catch (Exception e) {
            log.error("Error processing InvoiceCreatedEvent", e);
        }
    }

    /**
     * Listen to invoice updated events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleInvoiceUpdatedEvent(InvoiceUpdatedEvent event) {
        log.info("Received InvoiceUpdatedEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("Invoice Updated")
                    .message(String.format("Invoice %s has been updated: %s changed from %s to %s", 
                            event.getInvoiceNumber(), event.getUpdateType(), event.getOldValue(), event.getNewValue()))
                    .type(NotificationType.INVOICE_UPDATED)
                    .priority(NotificationPriority.LOW)
                    .relatedEntityId(event.getInvoiceId())
                    .relatedEntityType("INVOICE")
                    .actionUrl("/invoices/" + event.getInvoiceId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for InvoiceUpdatedEvent");
        } catch (Exception e) {
            log.error("Error processing InvoiceUpdatedEvent", e);
        }
    }

    /**
     * Listen to invoice paid events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleInvoicePaidEvent(InvoicePaidEvent event) {
        log.info("Received InvoicePaidEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("Invoice Payment Received")
                    .message(String.format("Payment of $%.2f received for Invoice %s via %s", 
                            event.getAmount(), event.getInvoiceNumber(), event.getPaymentMethod()))
                    .type(NotificationType.INVOICE_PAID)
                    .priority(NotificationPriority.HIGH)
                    .relatedEntityId(event.getInvoiceId())
                    .relatedEntityType("INVOICE")
                    .actionUrl("/invoices/" + event.getInvoiceId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for InvoicePaidEvent");
        } catch (Exception e) {
            log.error("Error processing InvoicePaidEvent", e);
        }
    }

    /**
     * Listen to task assigned events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleTaskAssignedEvent(TaskAssignedEvent event) {
        log.info("Received TaskAssignedEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("New Task Assigned")
                    .message(String.format("You have been assigned a new task: %s by %s. Due date: %s", 
                            event.getTaskTitle(), event.getAssignedBy(), event.getDueDate()))
                    .type(NotificationType.TASK_ASSIGNED)
                    .priority(parsePriority(event.getPriority()))
                    .relatedEntityId(event.getTaskId())
                    .relatedEntityType("TASK")
                    .actionUrl("/tasks/" + event.getTaskId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for TaskAssignedEvent");
        } catch (Exception e) {
            log.error("Error processing TaskAssignedEvent", e);
        }
    }

    /**
     * Listen to task completed events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleTaskCompletedEvent(TaskCompletedEvent event) {
        log.info("Received TaskCompletedEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("Task Completed")
                    .message(String.format("Task '%s' has been completed by %s", 
                            event.getTaskTitle(), event.getCompletedBy()))
                    .type(NotificationType.TASK_COMPLETED)
                    .priority(NotificationPriority.MEDIUM)
                    .relatedEntityId(event.getTaskId())
                    .relatedEntityType("TASK")
                    .actionUrl("/tasks/" + event.getTaskId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for TaskCompletedEvent");
        } catch (Exception e) {
            log.error("Error processing TaskCompletedEvent", e);
        }
    }

    /**
     * Listen to vehicle booking created events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleVehicleBookingCreatedEvent(VehicleBookingCreatedEvent event) {
        log.info("Received VehicleBookingCreatedEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("Booking Created")
                    .message(String.format("Your booking for %s has been created. Total: $%.2f. Pickup: %s", 
                            event.getVehicleName(), event.getTotalAmount(), 
                            event.getBookingStartDate().toLocalDate()))
                    .type(NotificationType.BOOKING_CREATED)
                    .priority(NotificationPriority.HIGH)
                    .relatedEntityId(event.getBookingId())
                    .relatedEntityType("BOOKING")
                    .actionUrl("/bookings/" + event.getBookingId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for VehicleBookingCreatedEvent");
        } catch (Exception e) {
            log.error("Error processing VehicleBookingCreatedEvent", e);
        }
    }

    /**
     * Listen to vehicle booking confirmed events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleVehicleBookingConfirmedEvent(VehicleBookingConfirmedEvent event) {
        log.info("Received VehicleBookingConfirmedEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("Booking Confirmed")
                    .message(String.format("Your booking for %s is confirmed! Confirmation #: %s. Pickup at %s on %s", 
                            event.getVehicleName(), event.getConfirmationNumber(),
                            event.getPickupLocation(), event.getPickupTime().toLocalDate()))
                    .type(NotificationType.BOOKING_CONFIRMED)
                    .priority(NotificationPriority.HIGH)
                    .relatedEntityId(event.getBookingId())
                    .relatedEntityType("BOOKING")
                    .actionUrl("/bookings/" + event.getBookingId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for VehicleBookingConfirmedEvent");
        } catch (Exception e) {
            log.error("Error processing VehicleBookingConfirmedEvent", e);
        }
    }

    /**
     * Listen to vehicle maintenance scheduled events
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleVehicleMaintenanceScheduledEvent(VehicleMaintenanceScheduledEvent event) {
        log.info("Received VehicleMaintenanceScheduledEvent: {}", event);
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("Maintenance Scheduled")
                    .message(String.format("Maintenance scheduled for %s: %s on %s at %s. Estimated cost: $%.2f", 
                            event.getVehicleName(), event.getMaintenanceType(),
                            event.getScheduledDate().toLocalDate(), event.getServiceCenter(),
                            event.getEstimatedCost()))
                    .type(NotificationType.MAINTENANCE_SCHEDULED)
                    .priority(NotificationPriority.MEDIUM)
                    .relatedEntityId(event.getMaintenanceId())
                    .relatedEntityType("MAINTENANCE")
                    .actionUrl("/maintenance/" + event.getMaintenanceId())
                    .build();
            
            notificationService.createNotification(request);
            log.info("Successfully created notification for VehicleMaintenanceScheduledEvent");
        } catch (Exception e) {
            log.error("Error processing VehicleMaintenanceScheduledEvent", e);
        }
    }

    /**
     * Helper method to parse priority string to enum
     */
    private NotificationPriority parsePriority(String priority) {
        try {
            return NotificationPriority.valueOf(priority.toUpperCase());
        } catch (Exception e) {
            return NotificationPriority.MEDIUM;
        }
    }
}
