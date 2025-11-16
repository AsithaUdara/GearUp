package com.gearup.notificationservice.factory;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;

public class NotificationFactory {

    private NotificationFactory() {
    }

    public static NotificationRequest createPaymentCompletedNotification(
            String userId, String paymentId, String transactionId, Double amount, String paymentMethod) {
        return NotificationRequest.builder()
                .userId(userId)
                .title("Payment Successful")
                .message(String.format("Your payment of $%.2f has been processed successfully. Transaction ID: %s",
                        amount, transactionId))
                .type(NotificationType.PAYMENT_COMPLETED)
                .priority(NotificationPriority.HIGH)
                .relatedEntityId(paymentId)
                .relatedEntityType("PAYMENT")
                .actionUrl("/payments/" + paymentId)
                .build();
    }

    public static NotificationRequest createInvoiceCreatedNotification(
            String userId, String invoiceId, String invoiceNumber, Double amount) {
        return NotificationRequest.builder()
                .userId(userId)
                .title("New Invoice Generated")
                .message(String.format("Invoice %s has been created for $%.2f. Please review and pay at your earliest convenience.",
                        invoiceNumber, amount))
                .type(NotificationType.INVOICE_CREATED)
                .priority(NotificationPriority.MEDIUM)
                .relatedEntityId(invoiceId)
                .relatedEntityType("INVOICE")
                .actionUrl("/invoices/" + invoiceId)
                .build();
    }

    public static NotificationRequest createInvoicePaidNotification(
            String userId, String invoiceId, String invoiceNumber, Double amount) {
        return NotificationRequest.builder()
                .userId(userId)
                .title("Invoice Paid - Thank You!")
                .message(String.format("Invoice %s has been paid ($%.2f). Thank you for your payment!",
                        invoiceNumber, amount))
                .type(NotificationType.INVOICE_PAID)
                .priority(NotificationPriority.HIGH)
                .relatedEntityId(invoiceId)
                .relatedEntityType("INVOICE")
                .actionUrl("/invoices/" + invoiceId)
                .build();
    }

    public static NotificationRequest createCustomerRegisteredNotification(
            String customerId, String displayName, String email) {
        return NotificationRequest.builder()
                .userId(customerId)
                .title("Welcome to GearUp!")
                .message(String.format("Welcome %s! Your account has been successfully created. Complete your profile to get started.",
                        displayName != null && !displayName.isEmpty() ? displayName : "to GearUp"))
                .type(NotificationType.CUSTOMER_REGISTERED)
                .priority(NotificationPriority.MEDIUM)
                .relatedEntityId(customerId)
                .relatedEntityType("CUSTOMER")
                .actionUrl("/profile/setup")
                .build();
    }

    public static NotificationRequest createCustomerKycChangedNotification(
            String customerId, String newStatus, String reason) {
        String message = String.format("Your KYC verification status has been updated to: %s", newStatus);
        if (reason != null && !reason.isEmpty()) {
            message += String.format(". Reason: %s", reason);
        }

        return NotificationRequest.builder()
                .userId(customerId)
                .title("KYC Status Updated")
                .message(message)
                .type(NotificationType.CUSTOMER_KYC_CHANGED)
                .priority(NotificationPriority.HIGH)
                .relatedEntityId(customerId)
                .relatedEntityType("CUSTOMER")
                .actionUrl("/profile/kyc")
                .build();
    }

    public static NotificationRequest createUserRegisteredNotification(
            String userId, String userName, String userEmail, String userRole) {
        return NotificationRequest.builder()
                .userId(userId)
                .title("Welcome to GearUp Team!")
                .message(String.format("Welcome %s! Your account has been created with role: %s. Check your email (%s) for login credentials and training resources.",
                        userName != null && !userName.isEmpty() ? userName : "to the team",
                        userRole,
                        userEmail))
                .type(NotificationType.USER_REGISTERED)
                .priority(NotificationPriority.MEDIUM)
                .relatedEntityId(userId)
                .relatedEntityType("USER")
                .actionUrl("/dashboard")
                .build();
    }

    public static NotificationRequest createRoleAssignedNotification(
            String userId, String roleName, String assignedBy) {
        return NotificationRequest.builder()
                .userId(userId)
                .title("Role Updated")
                .message(String.format("Your role has been updated to: %s by %s. Your new permissions are now active.",
                        roleName,
                        assignedBy != null && !assignedBy.isEmpty() ? assignedBy : "Administrator"))
                .type(NotificationType.ROLE_ASSIGNED)
                .priority(NotificationPriority.HIGH)
                .relatedEntityId(userId)
                .relatedEntityType("USER")
                .actionUrl("/profile/permissions")
                .build();
    }

    public static NotificationRequest createVehicleRegisteredNotification(
            String customerId, Long vehicleId, String make, String model, Integer year) {
        return NotificationRequest.builder()
                .userId(customerId)
                .title("Vehicle Registered Successfully")
                .message(String.format("Your vehicle %s %s %s has been registered. You can now book services for this vehicle.",
                        year != null ? year : "",
                        make,
                        model))
                .type(NotificationType.VEHICLE_REGISTERED)
                .priority(NotificationPriority.MEDIUM)
                .relatedEntityId(String.valueOf(vehicleId))
                .relatedEntityType("VEHICLE")
                .actionUrl("/vehicles/" + vehicleId)
                .build();
    }

    public static NotificationRequest createVehicleUpdatedNotification(
            String customerId, Long vehicleId, String updatedField) {
        return NotificationRequest.builder()
                .userId(customerId)
                .title("Vehicle Information Updated")
                .message(String.format("Your vehicle information has been updated successfully%s.",
                        updatedField != null && !updatedField.isEmpty() ? " (" + updatedField + ")" : ""))
                .type(NotificationType.VEHICLE_UPDATED)
                .priority(NotificationPriority.LOW)
                .relatedEntityId(String.valueOf(vehicleId))
                .relatedEntityType("VEHICLE")
                .actionUrl("/vehicles/" + vehicleId)
                .build();
    }
}
