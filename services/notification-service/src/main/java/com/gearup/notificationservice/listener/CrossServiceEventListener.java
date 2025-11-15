package com.gearup.notificationservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

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

    // ==================== PAYMENT EVENTS ====================
    
    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Payment completed: paymentId={}, amount={}", 
                    event.getPaymentId(), event.getAmount());
            
            // TODO: Send notification to customer
            // - "Your payment of $X has been processed successfully"
            // - Include transaction ID and receipt
            
        } catch (Exception e) {
            log.error("❌ Failed to process PaymentCompletedEvent", e);
        }
    }

    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Invoice created: invoiceId={}, amount={}", 
                    event.getInvoiceId(), event.getAmount());
            
            // TODO: Send notification to customer
            // - "New invoice generated: #INV-XXX for $Y"
            // - Include payment link
            
        } catch (Exception e) {
            log.error("❌ Failed to process InvoiceCreatedEvent", e);
        }
    }

    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleInvoicePaid(InvoicePaidEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Invoice paid: invoiceId={}, amount={}", 
                    event.getInvoiceId(), event.getAmount());
            
            // TODO: Send notification to customer
            // - "Invoice #INV-XXX has been paid. Thank you!"
            
        } catch (Exception e) {
            log.error("❌ Failed to process InvoicePaidEvent", e);
        }
    }

    // ==================== CUSTOMER EVENTS ====================
    
    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleCustomerRegistered(CustomerRegisteredEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Customer registered: customerId={}, email={}", 
                    event.getCustomerId(), event.getEmail());
            
            // TODO: Send welcome notification
            // - Welcome email with onboarding guide
            // - Setup profile reminder
            
        } catch (Exception e) {
            log.error("❌ Failed to process CustomerRegisteredEvent", e);
        }
    }

    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleCustomerKycChanged(CustomerKycChangedEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Customer KYC changed: customerId={}, newStatus={}", 
                    event.getCustomerId(), event.getNewStatus());
            
            // TODO: Send KYC status notification
            // - "Your KYC verification is now: [Status]"
            // - Next steps if needed
            
        } catch (Exception e) {
            log.error("❌ Failed to process CustomerKycChangedEvent", e);
        }
    }

    // ==================== USER EVENTS ====================
    
    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] User registered: userId={}, email={}, role={}", 
                    event.getUserId(), event.getUserEmail(), event.getUserRole());
            
            // TODO: Send welcome notification to new employee
            // - Welcome email with login credentials
            // - Training resources
            
        } catch (Exception e) {
            log.error("❌ Failed to process UserRegisteredEvent", e);
        }
    }

    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleRoleAssigned(RoleAssignedEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Role assigned: userId={}, newRole={}", 
                    event.getUserId(), event.getRoleName());
            
            // TODO: Send role change notification
            // - "Your role has been updated to: [New Role]"
            // - New permissions summary
            
        } catch (Exception e) {
            log.error("❌ Failed to process RoleAssignedEvent", e);
        }
    }

    // ==================== VEHICLE EVENTS ====================
    
    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleVehicleRegistered(VehicleRegisteredEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Vehicle registered: vehicleId={}, make={}, model={}", 
                    event.getVehicleId(), event.getMake(), event.getModel());
            
            // TODO: Send vehicle registration confirmation
            // - "Your vehicle has been registered: [Make Model]"
            // - Next steps for booking services
            
        } catch (Exception e) {
            log.error("❌ Failed to process VehicleRegisteredEvent", e);
        }
    }

    @RabbitListener(queues = "#{notificationQueue.name}")
    public void handleVehicleUpdated(VehicleUpdatedEvent event) {
        try {
            log.info("🔔 [NOTIFICATION] Vehicle updated: vehicleId={}", event.getVehicleId());
            
            // TODO: Send vehicle update confirmation
            // - "Your vehicle information has been updated"
            
        } catch (Exception e) {
            log.error("❌ Failed to process VehicleUpdatedEvent", e);
        }
    }
}
