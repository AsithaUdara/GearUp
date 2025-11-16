package com.gearup.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.payment.InvoiceCreatedEvent;
import com.gearup.shared.event.payment.InvoicePaidEvent;
import com.gearup.shared.event.payment.InvoiceUpdatedEvent;
import com.gearup.shared.event.tracking.TaskAssignedEvent;
import com.gearup.shared.event.tracking.TaskCompletedEvent;
import com.gearup.shared.messaging.RabbitMQConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Component  // Temporarily disabled - events handled by consolidated dispatcher
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleMessage(org.springframework.messaging.Message<?> message) {
        Object payload = message.getPayload();

        try {
            if (payload instanceof InvoiceCreatedEvent) {
                handleInvoiceCreatedEvent((InvoiceCreatedEvent) payload);
            } else if (payload instanceof InvoiceUpdatedEvent) {
                handleInvoiceUpdatedEvent((InvoiceUpdatedEvent) payload);
            } else if (payload instanceof InvoicePaidEvent) {
                handleInvoicePaidEvent((InvoicePaidEvent) payload);
            } else if (payload instanceof TaskAssignedEvent) {
                handleTaskAssignedEvent((TaskAssignedEvent) payload);
            } else if (payload instanceof TaskCompletedEvent) {
                handleTaskCompletedEvent((TaskCompletedEvent) payload);
            }
            // Silently ignore other event types
        } catch (Exception e) {
            log.error("Error processing notification event: {}", payload.getClass().getName(), e);
            throw e;
        }
    }

    /**
     * Handle invoice created events
     */
    private void handleInvoiceCreatedEvent(InvoiceCreatedEvent event) {
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
     * Handle invoice updated events
     */
    private void handleInvoiceUpdatedEvent(InvoiceUpdatedEvent event) {
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
     * Handle invoice paid events
     */
    private void handleInvoicePaidEvent(InvoicePaidEvent event) {
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
     * Handle task assigned events
     */
    private void handleTaskAssignedEvent(TaskAssignedEvent event) {
        log.info("Received TaskAssignedEvent: {}", event);

        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getAssigneeId())
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
     * Handle task completed events
     */
    private void handleTaskCompletedEvent(TaskCompletedEvent event) {
        log.info("Received TaskCompletedEvent: {}", event);

        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getAssigneeId())
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
