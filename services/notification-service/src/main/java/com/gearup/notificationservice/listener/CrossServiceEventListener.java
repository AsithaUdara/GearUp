package com.gearup.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.factory.NotificationFactory;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.customer.CustomerKycChangedEvent;
import com.gearup.shared.event.customer.CustomerRegisteredEvent;
import com.gearup.shared.event.payment.InvoiceCreatedEvent;
import com.gearup.shared.event.payment.InvoicePaidEvent;
import com.gearup.shared.event.payment.PaymentCompletedEvent;
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
}
