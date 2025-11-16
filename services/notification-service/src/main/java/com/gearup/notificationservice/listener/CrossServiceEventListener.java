package com.gearup.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.factory.NotificationFactory;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.appointment.AppointmentApprovedEvent;
import com.gearup.shared.event.appointment.AppointmentCancelledEvent;
import com.gearup.shared.event.appointment.AppointmentCreatedEvent;
import com.gearup.shared.event.appointment.EmployeeAssignedToAppointmentEvent;
import com.gearup.shared.event.chatbot.ChatSessionClosedEvent;
import com.gearup.shared.event.chatbot.ChatSessionStartedEvent;
import com.gearup.shared.event.chatbot.CustomerEscalationRequestedEvent;
import com.gearup.shared.event.customer.CustomerKycChangedEvent;
import com.gearup.shared.event.customer.CustomerRegisteredEvent;
import com.gearup.shared.event.modification.ModificationApprovedEvent;
import com.gearup.shared.event.modification.ModificationCancelledEvent;
import com.gearup.shared.event.modification.ModificationCompletedEvent;
import com.gearup.shared.event.modification.ModificationRejectedEvent;
import com.gearup.shared.event.modification.ModificationRequestCreatedEvent;
import com.gearup.shared.event.modification.ModificationRequestStatusChangedEvent;
import com.gearup.shared.event.parts.PartsInventoryLowEvent;
import com.gearup.shared.event.parts.PartsStatusChangedEvent;
import com.gearup.shared.event.payment.InvoiceCreatedEvent;
import com.gearup.shared.event.payment.InvoicePaidEvent;
import com.gearup.shared.event.payment.PaymentCompletedEvent;
import com.gearup.shared.event.tracking.TaskAssignedEvent;
import com.gearup.shared.event.tracking.TaskCompletedEvent;
import com.gearup.shared.event.user.RoleAssignedEvent;
import com.gearup.shared.event.user.UserRegisteredEvent;
import com.gearup.shared.event.vehicle.VehicleRegisteredEvent;
import com.gearup.shared.event.vehicle.VehicleUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event listener for cross-service notifications
 * Listens to payment, customer, user, and vehicle events to send notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CrossServiceEventListener {

    private final NotificationService notificationService;

    // ==================== PAYMENT EVENTS ====================
    
    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleMessage(org.springframework.messaging.Message<?> message) {
        Object payload = message.getPayload();

        try {
            if (payload instanceof PaymentCompletedEvent) {
                handlePaymentCompleted((PaymentCompletedEvent) payload);
            } else if (payload instanceof InvoiceCreatedEvent) {
                handleInvoiceCreated((InvoiceCreatedEvent) payload);
            } else if (payload instanceof InvoicePaidEvent) {
                handleInvoicePaid((InvoicePaidEvent) payload);
            } else if (payload instanceof CustomerRegisteredEvent) {
                handleCustomerRegistered((CustomerRegisteredEvent) payload);
            } else if (payload instanceof CustomerKycChangedEvent) {
                handleCustomerKycChanged((CustomerKycChangedEvent) payload);
            } else if (payload instanceof UserRegisteredEvent) {
                handleUserRegistered((UserRegisteredEvent) payload);
            } else if (payload instanceof RoleAssignedEvent) {
                handleRoleAssigned((RoleAssignedEvent) payload);
            } else if (payload instanceof VehicleRegisteredEvent) {
                handleVehicleRegistered((VehicleRegisteredEvent) payload);
            } else if (payload instanceof VehicleUpdatedEvent) {
                handleVehicleUpdated((VehicleUpdatedEvent) payload);
            } else if (payload instanceof AppointmentCreatedEvent) {
                handleAppointmentCreated((AppointmentCreatedEvent) payload);
            } else if (payload instanceof AppointmentApprovedEvent) {
                handleAppointmentApproved((AppointmentApprovedEvent) payload);
            } else if (payload instanceof AppointmentCancelledEvent) {
                handleAppointmentCancelled((AppointmentCancelledEvent) payload);
            } else if (payload instanceof EmployeeAssignedToAppointmentEvent) {
                handleEmployeeAssigned((EmployeeAssignedToAppointmentEvent) payload);
            } else if (payload instanceof ModificationRequestCreatedEvent) {
                handleModificationRequestCreated((ModificationRequestCreatedEvent) payload);
            } else if (payload instanceof ModificationApprovedEvent) {
                handleModificationApproved((ModificationApprovedEvent) payload);
            } else if (payload instanceof ModificationRejectedEvent) {
                handleModificationRejected((ModificationRejectedEvent) payload);
            } else if (payload instanceof ModificationCompletedEvent) {
                handleModificationCompleted((ModificationCompletedEvent) payload);
            } else if (payload instanceof ModificationCancelledEvent) {
                handleModificationCancelled((ModificationCancelledEvent) payload);
            } else if (payload instanceof ModificationRequestStatusChangedEvent) {
                handleModificationStatusChanged((ModificationRequestStatusChangedEvent) payload);
            } else if (payload instanceof PartsInventoryLowEvent) {
                handlePartsInventoryLow((PartsInventoryLowEvent) payload);
            } else if (payload instanceof PartsStatusChangedEvent) {
                handlePartsStatusChanged((PartsStatusChangedEvent) payload);
            } else if (payload instanceof ChatSessionStartedEvent) {
                handleChatSessionStarted((ChatSessionStartedEvent) payload);
            } else if (payload instanceof ChatSessionClosedEvent) {
                handleChatSessionClosed((ChatSessionClosedEvent) payload);
            } else if (payload instanceof CustomerEscalationRequestedEvent) {
                handleCustomerEscalationRequested((CustomerEscalationRequestedEvent) payload);
            } else if (payload instanceof TaskCompletedEvent) {
                handleTaskCompleted((TaskCompletedEvent) payload);
            } else if (payload instanceof TaskAssignedEvent) {
                handleTaskAssigned((TaskAssignedEvent) payload);
            } else {
                log.warn("Received unknown event type: {}", payload.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", payload.getClass().getName(), e);
            throw e;
        }
    }

    // ==================== PAYMENT EVENTS ====================

    private void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("[NOTIFICATION] Payment completed: paymentId={}, amount={}",
                    event.getPaymentId(), event.getAmount());

            NotificationRequest request = NotificationFactory.createPaymentCompletedNotification(
                    event.getUserId(),
                    event.getPaymentId(),
                    event.getTransactionId(),
                    event.getAmount(),
                    event.getPaymentMethod()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for PaymentCompletedEvent");

        } catch (Exception e) {
            log.error("Failed to process PaymentCompletedEvent", e);
        }
    }

    private void handleInvoiceCreated(InvoiceCreatedEvent event) {
        try {
            log.info("[NOTIFICATION] Invoice created: invoiceId={}, amount={}",
                    event.getInvoiceId(), event.getAmount());

            NotificationRequest request = NotificationFactory.createInvoiceCreatedNotification(
                    event.getUserId(),
                    event.getInvoiceId(),
                    event.getInvoiceNumber(),
                    event.getAmount()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for InvoiceCreatedEvent");

        } catch (Exception e) {
            log.error("Failed to process InvoiceCreatedEvent", e);
        }
    }

    private void handleInvoicePaid(InvoicePaidEvent event) {
        try {
            log.info("[NOTIFICATION] Invoice paid: invoiceId={}, amount={}",
                    event.getInvoiceId(), event.getAmount());

            NotificationRequest request = NotificationFactory.createInvoicePaidNotification(
                    event.getUserId(),
                    event.getInvoiceId(),
                    event.getInvoiceNumber(),
                    event.getAmount()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for InvoicePaidEvent");

        } catch (Exception e) {
            log.error("Failed to process InvoicePaidEvent", e);
        }
    }

    // ==================== CUSTOMER EVENTS ====================
    
    private void handleCustomerRegistered(CustomerRegisteredEvent event) {
        try {
            log.info("[NOTIFICATION] Customer registered: customerId={}, email={}",
                    event.getCustomerId(), event.getEmail());

            NotificationRequest request = NotificationFactory.createCustomerRegisteredNotification(
                    event.getCustomerId(),
                    event.getDisplayName(),
                    event.getEmail()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for CustomerRegisteredEvent");

        } catch (Exception e) {
            log.error("Failed to process CustomerRegisteredEvent", e);
        }
    }

    private void handleCustomerKycChanged(CustomerKycChangedEvent event) {
        try {
            log.info("[NOTIFICATION] Customer KYC changed: customerId={}, newStatus={}",
                    event.getCustomerId(), event.getNewStatus());

            NotificationRequest request = NotificationFactory.createCustomerKycChangedNotification(
                    event.getCustomerId(),
                    event.getNewStatus(),
                    event.getReason()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for CustomerKycChangedEvent");

        } catch (Exception e) {
            log.error("Failed to process CustomerKycChangedEvent", e);
        }
    }

    // ==================== USER EVENTS ====================
    
    private void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("[NOTIFICATION] User registered: userId={}, email={}, role={}",
                    event.getUserId(), event.getUserEmail(), event.getUserRole());

            NotificationRequest request = NotificationFactory.createUserRegisteredNotification(
                    event.getUserId(),
                    event.getUserName(),
                    event.getUserEmail(),
                    event.getUserRole()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for UserRegisteredEvent");

        } catch (Exception e) {
            log.error("Failed to process UserRegisteredEvent", e);
        }
    }

    private void handleRoleAssigned(RoleAssignedEvent event) {
        try {
            log.info("[NOTIFICATION] Role assigned: userId={}, newRole={}",
                    event.getUserId(), event.getRoleName());

            NotificationRequest request = NotificationFactory.createRoleAssignedNotification(
                    event.getUserId(),
                    event.getRoleName(),
                    event.getAssignedBy()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for RoleAssignedEvent");

        } catch (Exception e) {
            log.error("Failed to process RoleAssignedEvent", e);
        }
    }

    // ==================== VEHICLE EVENTS ====================
    
    private void handleVehicleRegistered(VehicleRegisteredEvent event) {
        try {
            log.info("[NOTIFICATION] Vehicle registered: vehicleId={}, make={}, model={}",
                    event.getVehicleId(), event.getMake(), event.getModel());

            NotificationRequest request = NotificationFactory.createVehicleRegisteredNotification(
                    event.getCustomerId(),
                    event.getVehicleId(),
                    event.getMake(),
                    event.getModel(),
                    event.getYear()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for VehicleRegisteredEvent");

        } catch (Exception e) {
            log.error("Failed to process VehicleRegisteredEvent", e);
        }
    }

    private void handleVehicleUpdated(VehicleUpdatedEvent event) {
        try {
            log.info("[NOTIFICATION] Vehicle updated: vehicleId={}", event.getVehicleId());

            NotificationRequest request = NotificationFactory.createVehicleUpdatedNotification(
                    event.getCustomerId(),
                    event.getVehicleId(),
                    event.getUpdatedField()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for VehicleUpdatedEvent");

        } catch (Exception e) {
            log.error("Failed to process VehicleUpdatedEvent", e);
        }
    }

    // ==================== APPOINTMENT EVENTS ====================

    private void handleAppointmentCreated(AppointmentCreatedEvent event) {
        try {
            log.info("[NOTIFICATION] Appointment created: bookingId={}, customerId={}, serviceName={}",
                    event.getBookingId(), event.getCustomerId(), event.getServiceName());

                NotificationRequest request = NotificationFactory.createAppointmentCreatedNotification(
                    event.getCustomerId(),
                    event.getBookingId(),
                    event.getServiceName(),
                    event.getSlotDateTime() != null ? event.getSlotDateTime().toString() : ""
                );

            notificationService.createNotification(request);
            log.info("Successfully created notification for AppointmentCreatedEvent");

        } catch (Exception e) {
            log.error("Failed to process AppointmentCreatedEvent", e);
        }
    }

    private void handleAppointmentApproved(AppointmentApprovedEvent event) {
        try {
            log.info("[NOTIFICATION] Appointment approved: bookingId={}, customerId={}",
                    event.getBookingId(), event.getCustomerId());

                NotificationRequest request = NotificationFactory.createAppointmentApprovedNotification(
                    event.getCustomerId(),
                    event.getBookingId(),
                    event.getServiceName(),
                    event.getApprovedAt() != null ? event.getApprovedAt().toString() : ""
                );

            notificationService.createNotification(request);
            log.info("Successfully created notification for AppointmentApprovedEvent");

        } catch (Exception e) {
            log.error("Failed to process AppointmentApprovedEvent", e);
        }
    }

    private void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        try {
            log.info("[NOTIFICATION] Appointment cancelled: bookingId={}, customerId={}, reason={}",
                    event.getBookingId(), event.getCustomerId(), event.getCancellationReason());

            NotificationRequest request = NotificationFactory.createAppointmentCancelledNotification(
                    event.getCustomerId(),
                    event.getBookingId(),
                    event.getCancellationReason()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for AppointmentCancelledEvent");

        } catch (Exception e) {
            log.error("Failed to process AppointmentCancelledEvent", e);
        }
    }

    private void handleEmployeeAssigned(EmployeeAssignedToAppointmentEvent event) {
        try {
            log.info("[NOTIFICATION] Employee assigned: bookingId={}, employeeId={}, employeeName={}",
                    event.getBookingId(), event.getEmployeeId(), event.getEmployeeName());

            NotificationRequest request = NotificationFactory.createEmployeeAssignedNotification(
                    event.getCustomerId(),
                    event.getBookingId(),
                    event.getEmployeeName(),
                    event.getEmployeeEmail() != null ? event.getEmployeeEmail() : "Contact information not available"
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for EmployeeAssignedToAppointmentEvent");

        } catch (Exception e) {
            log.error("Failed to process EmployeeAssignedToAppointmentEvent", e);
        }
    }

    // ==================== MODIFICATION EVENTS ====================

    private void handleModificationRequestCreated(ModificationRequestCreatedEvent event) {
        try {
            log.info("[NOTIFICATION] Modification request created: modificationId={}, customerId={}, serviceName={}",
                    event.getModificationId(), event.getCustomerId(), event.getServiceName());

            NotificationRequest request = NotificationFactory.createModificationRequestCreatedNotification(
                    event.getCustomerId(),
                    event.getModificationId(),
                    event.getServiceName(),
                    event.getEstimatedCost()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for ModificationRequestCreatedEvent");

        } catch (Exception e) {
            log.error("Failed to process ModificationRequestCreatedEvent", e);
        }
    }

    private void handleModificationApproved(ModificationApprovedEvent event) {
        try {
            log.info("[NOTIFICATION] Modification approved: modificationId={}, customerId={}",
                    event.getModificationId(), event.getCustomerId());

            NotificationRequest request = NotificationFactory.createModificationApprovedNotification(
                    event.getCustomerId(),
                    event.getModificationId(),
                    event.getModificationType()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for ModificationApprovedEvent");

        } catch (Exception e) {
            log.error("Failed to process ModificationApprovedEvent", e);
        }
    }

    private void handleModificationRejected(ModificationRejectedEvent event) {
        try {
            log.info("[NOTIFICATION] Modification rejected: modificationId={}, customerId={}, reason={}",
                    event.getModificationId(), event.getCustomerId(), event.getRejectionReason());

            NotificationRequest request = NotificationFactory.createModificationRejectedNotification(
                    event.getCustomerId(),
                    event.getModificationId(),
                    event.getModificationType(),
                    event.getRejectionReason()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for ModificationRejectedEvent");

        } catch (Exception e) {
            log.error("Failed to process ModificationRejectedEvent", e);
        }
    }

    private void handleModificationCompleted(ModificationCompletedEvent event) {
        try {
            log.info("[NOTIFICATION] Modification completed: modificationId={}, customerId={}, finalCost={}",
                    event.getModificationId(), event.getCustomerId(), event.getFinalCost());

            NotificationRequest request = NotificationFactory.createModificationCompletedNotification(
                    event.getCustomerId(),
                    event.getModificationId(),
                    event.getModificationType(),
                    event.getFinalCost()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for ModificationCompletedEvent");

        } catch (Exception e) {
            log.error("Failed to process ModificationCompletedEvent", e);
        }
    }

    private void handleModificationCancelled(ModificationCancelledEvent event) {
        try {
            log.info("[NOTIFICATION] Modification cancelled: modificationId={}, customerId={}, reason={}",
                    event.getModificationId(), event.getCustomerId(), event.getCancellationReason());

            NotificationRequest request = NotificationFactory.createModificationCancelledNotification(
                    event.getCustomerId(),
                    event.getModificationId(),
                    event.getModificationType(),
                    event.getCancellationReason()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for ModificationCancelledEvent");

        } catch (Exception e) {
            log.error("Failed to process ModificationCancelledEvent", e);
        }
    }

    private void handleModificationStatusChanged(ModificationRequestStatusChangedEvent event) {
        try {
            log.info("[NOTIFICATION] Modification status changed: modificationId={}, oldStatus={}, newStatus={}",
                    event.getModificationId(), event.getOldStatus(), event.getNewStatus());

            // Only send notification for status changes that aren't covered by other specific events
            if (!"APPROVED".equals(event.getNewStatus()) &&
                !"REJECTED".equals(event.getNewStatus()) &&
                !"COMPLETED".equals(event.getNewStatus()) &&
                !"CANCELLED".equals(event.getNewStatus())) {

                NotificationRequest request = NotificationFactory.createModificationStatusChangedNotification(
                        event.getCustomerId(),
                        event.getModificationId(),
                        event.getServiceName(),
                        event.getNewStatus()
                );

                notificationService.createNotification(request);
                log.info("Successfully created notification for ModificationRequestStatusChangedEvent");
            }

        } catch (Exception e) {
            log.error("Failed to process ModificationRequestStatusChangedEvent", e);
        }
    }

    // ==================== PARTS EVENTS ====================

    private void handlePartsInventoryLow(PartsInventoryLowEvent event) {
        try {
            log.info("[NOTIFICATION] Parts inventory low: partId={}, partName={}, currentQuantity={}, minimumQuantity={}",
                    event.getPartId(), event.getPartName(), event.getCurrentQuantity(), event.getMinimumQuantity());

            NotificationRequest request = NotificationFactory.createPartsInventoryLowNotification(
                    event.getPartId(),
                    event.getPartName(),
                    event.getCurrentQuantity(),
                    event.getMinimumQuantity()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for PartsInventoryLowEvent");

        } catch (Exception e) {
            log.error("Failed to process PartsInventoryLowEvent", e);
        }
    }

    private void handlePartsStatusChanged(PartsStatusChangedEvent event) {
        try {
            log.info("[NOTIFICATION] Parts status changed: partId={}, partName={}, oldStatus={}, newStatus={}",
                    event.getPartId(), event.getPartName(), event.getOldStatus(), event.getNewStatus());

            NotificationRequest request = NotificationFactory.createPartsStatusChangedNotification(
                    event.getPartId(),
                    event.getPartName(),
                    event.getOldStatus(),
                    event.getNewStatus()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for PartsStatusChangedEvent");

        } catch (Exception e) {
            log.error("Failed to process PartsStatusChangedEvent", e);
        }
    }

    // ==================== CHATBOT EVENTS ====================

    private void handleChatSessionStarted(ChatSessionStartedEvent event) {
        try {
            log.info("[NOTIFICATION] Chat session started: sessionId={}, customerId={}, customerName={}",
                    event.getSessionId(), event.getCustomerId(), event.getCustomerName());

                NotificationRequest request = NotificationFactory.createChatSessionStartedNotification(
                    event.getCustomerId(),
                    event.getSessionId(),
                    "Support Agent"
                );

            notificationService.createNotification(request);
            log.info("Successfully created notification for ChatSessionStartedEvent");

        } catch (Exception e) {
            log.error("Failed to process ChatSessionStartedEvent", e);
        }
    }

    private void handleChatSessionClosed(ChatSessionClosedEvent event) {
        try {
            log.info("[NOTIFICATION] Chat session closed: sessionId={}, customerId={}, durationMinutes={}",
                    event.getSessionId(), event.getCustomerId(), event.getDurationMinutes());

                NotificationRequest request = NotificationFactory.createChatSessionClosedNotification(
                    event.getCustomerId(),
                    event.getSessionId(),
                    event.getDurationMinutes(),
                    "Support Agent"
                );

            notificationService.createNotification(request);
            log.info("Successfully created notification for ChatSessionClosedEvent");

        } catch (Exception e) {
            log.error("Failed to process ChatSessionClosedEvent", e);
        }
    }

    private void handleCustomerEscalationRequested(CustomerEscalationRequestedEvent event) {
        try {
            log.info("[NOTIFICATION] Customer escalation requested: sessionId={}, customerId={}, urgency={}",
                    event.getSessionId(), event.getCustomerId(), event.getUrgencyLevel());

            // Send notification to customer
            NotificationRequest customerNotification = NotificationFactory.createCustomerEscalationRequestedNotification(
                    event.getCustomerId(),
                    event.getSessionId(),
                    event.getEscalationReason(),
                    event.getUrgencyLevel()
            );

            notificationService.createNotification(customerNotification);

            // Send alert to support team
            NotificationRequest supportAlert = new NotificationRequest();
            supportAlert.setUserId("SUPPORT_TEAM");
            supportAlert.setTitle("Customer Escalation Request");
            supportAlert.setMessage(String.format("Customer %s (ID: %s) has requested escalation. Reason: %s. Urgency: %s. Session ID: %s",
                    event.getCustomerName(), event.getCustomerId(), event.getEscalationReason(), event.getUrgencyLevel(), event.getSessionId()));
            supportAlert.setType(NotificationType.TASK_ASSIGNED);
            supportAlert.setPriority(determineEscalationPriority(event.getUrgencyLevel()));
            supportAlert.setRelatedEntityId(event.getSessionId());
            supportAlert.setRelatedEntityType("CHAT_SESSION");
            supportAlert.setDeliveryChannels(determineEscalationChannels(event.getUrgencyLevel()));

            notificationService.createNotification(supportAlert);
            log.info("Successfully created notification for CustomerEscalationRequestedEvent");

        } catch (Exception e) {
            log.error("Failed to process CustomerEscalationRequestedEvent", e);
        }
    }

    // ==================== TRACKING EVENTS ====================

    private void handleTaskCompleted(TaskCompletedEvent event) {
        try {
            log.info("[NOTIFICATION] Task completed: taskId={}, taskTitle={}, completedBy={}",
                    event.getTaskId(), event.getTaskTitle(), event.getCompletedBy());

            NotificationRequest request = NotificationFactory.createTaskCompletedNotification(
                    event.getAssigneeId(),
                    event.getTaskId(),
                    event.getTaskTitle(),
                    event.getCompletedBy()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for TaskCompletedEvent");

        } catch (Exception e) {
            log.error("Failed to process TaskCompletedEvent", e);
        }
    }

    private void handleTaskAssigned(TaskAssignedEvent event) {
        try {
            log.info("[NOTIFICATION] Task assigned: taskId={}, assigneeId={}, taskTitle={}",
                    event.getTaskId(), event.getAssigneeId(), event.getTaskTitle());

            NotificationRequest request = NotificationFactory.createTaskAssignedNotification(
                    event.getAssigneeId(),
                    event.getTaskId(),
                    event.getTaskTitle(),
                    event.getDueDate()
            );

            notificationService.createNotification(request);
            log.info("Successfully created notification for TaskAssignedEvent");

        } catch (Exception e) {
            log.error("Failed to process TaskAssignedEvent", e);
        }
    }

    // ==================== HELPER METHODS ====================

    private NotificationPriority determineEscalationPriority(String urgencyLevel) {
        return switch (urgencyLevel.toUpperCase()) {
            case "URGENT", "CRITICAL" -> NotificationPriority.HIGH;
            case "NORMAL" -> NotificationPriority.MEDIUM;
            default -> NotificationPriority.LOW;
        };
    }

    private String determineEscalationChannels(String urgencyLevel) {
        return switch (urgencyLevel.toUpperCase()) {
            case "URGENT", "CRITICAL" -> "[\"WEB\", \"EMAIL\", \"SMS\"]";
            case "NORMAL" -> "[\"WEB\", \"EMAIL\"]";
            default -> "[\"WEB\"]";
        };
    }
}
