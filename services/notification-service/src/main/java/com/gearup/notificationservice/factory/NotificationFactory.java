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

    // ==================== APPOINTMENT EVENTS ====================

    public static NotificationRequest createAppointmentCreatedNotification(
            String customerId, Long appointmentId, String serviceType, String appointmentDate) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Appointment Confirmed");
        request.setMessage(String.format("Your appointment for %s has been confirmed. Booking ID: #%d",
                serviceType, appointmentId));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.HIGH);
        request.setRelatedEntityId(appointmentId.toString());
        request.setRelatedEntityType("BOOKING");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
        return request;
    }

    public static NotificationRequest createAppointmentApprovedNotification(
            String customerId, Long appointmentId, String serviceType, String appointmentDate) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Appointment Approved");
        request.setMessage(String.format("Your %s appointment (Booking #%d) has been approved by management.",
                serviceType, appointmentId));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.MEDIUM);
        request.setRelatedEntityId(appointmentId.toString());
        request.setRelatedEntityType("BOOKING");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
        return request;
    }

    public static NotificationRequest createAppointmentCancelledNotification(
            String customerId, Long appointmentId, String cancellationReason) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Appointment Cancelled");
        request.setMessage(String.format("Your appointment (Booking #%d) has been cancelled. Reason: %s",
                appointmentId, cancellationReason));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.HIGH);
        request.setRelatedEntityId(appointmentId.toString());
        request.setRelatedEntityType("BOOKING");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");
        return request;
    }

    public static NotificationRequest createEmployeeAssignedNotification(
            String customerId, Long appointmentId, String employeeName, String employeeContact) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Technician Assigned");
        request.setMessage(String.format("%s has been assigned to your appointment. Contact: %s",
                employeeName, employeeContact));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.MEDIUM);
        request.setRelatedEntityId(appointmentId.toString());
        request.setRelatedEntityType("BOOKING");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");
        return request;
    }

    // ==================== MODIFICATION EVENTS ====================

    public static NotificationRequest createModificationRequestCreatedNotification(
            String customerId, Long modificationId, String serviceName, Double estimatedCost) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Modification Request Received");
        request.setMessage(String.format("Your modification request for %s has been received. Request ID: #%d. Estimated cost: $%.2f",
                serviceName, modificationId, estimatedCost));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.MEDIUM);
        request.setRelatedEntityId(modificationId.toString());
        request.setRelatedEntityType("MODIFICATION_REQUEST");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
        return request;
    }

    public static NotificationRequest createModificationApprovedNotification(
            String customerId, Long modificationId, String modificationType) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Modification Request Approved");
        request.setMessage(String.format("Great news! Your %s modification request (ID: #%d) has been approved. We'll contact you shortly to schedule the work.",
                modificationType, modificationId));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.HIGH);
        request.setRelatedEntityId(modificationId.toString());
        request.setRelatedEntityType("MODIFICATION_REQUEST");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");
        return request;
    }

    public static NotificationRequest createModificationRejectedNotification(
            String customerId, Long modificationId, String modificationType, String rejectionReason) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Modification Request Update");
        request.setMessage(String.format("Your %s modification request (ID: #%d) could not be approved at this time. Reason: %s. Please contact us for more information.",
                modificationType, modificationId, rejectionReason));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.HIGH);
        request.setRelatedEntityId(modificationId.toString());
        request.setRelatedEntityType("MODIFICATION_REQUEST");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
        return request;
    }

    public static NotificationRequest createModificationCompletedNotification(
            String customerId, Long modificationId, String modificationType, Double finalCost) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Modification Completed");
        request.setMessage(String.format("Your %s modification has been completed! Total cost: $%.2f. Thank you for choosing our services.",
                modificationType, finalCost));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.HIGH);
        request.setRelatedEntityId(modificationId.toString());
        request.setRelatedEntityType("MODIFICATION_REQUEST");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");
        return request;
    }

    public static NotificationRequest createModificationCancelledNotification(
            String customerId, Long modificationId, String modificationType, String cancellationReason) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Modification Request Cancelled");
        request.setMessage(String.format("Your %s modification request (ID: #%d) has been cancelled. Reason: %s",
                modificationType, modificationId, cancellationReason));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.MEDIUM);
        request.setRelatedEntityId(modificationId.toString());
        request.setRelatedEntityType("MODIFICATION_REQUEST");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
        return request;
    }

    public static NotificationRequest createModificationStatusChangedNotification(
            String customerId, Long modificationId, String serviceName, String newStatus) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Modification Request Status Update");
        request.setMessage(String.format("The status of your %s modification request (ID: #%d) has been updated to: %s",
                serviceName, modificationId, newStatus));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.LOW);
        request.setRelatedEntityId(modificationId.toString());
        request.setRelatedEntityType("MODIFICATION_REQUEST");
        request.setDeliveryChannels("[\"WEB\"]");
        return request;
    }

    // ==================== PARTS EVENTS ====================

    public static NotificationRequest createPartsInventoryLowNotification(
            Long partsId, String partsName, Integer currentStock, Integer reorderLevel) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId("PARTS_MANAGER");
        request.setTitle("Low Inventory Alert");
        request.setMessage(String.format("Part '%s' (ID: %d) is running low! Current stock: %d units (Minimum: %d). Please reorder immediately.",
                partsName, partsId, currentStock, reorderLevel));
        request.setType(NotificationType.SYSTEM_ALERT);
        request.setPriority(NotificationPriority.HIGH);
        request.setRelatedEntityId(partsId.toString());
        request.setRelatedEntityType("PARTS");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");
        return request;
    }

    public static NotificationRequest createPartsStatusChangedNotification(
            Long partsId, String partsName, String oldStatus, String newStatus) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId("PARTS_MANAGER");
        request.setTitle(String.format("Parts Status Updated: %s", partsName));
        request.setMessage(String.format("Part '%s' (ID: %d) status changed from %s to %s.",
                partsName, partsId, oldStatus, newStatus));
        request.setType(determinePartsType(newStatus));
        request.setPriority(determinePartsPriority(newStatus));
        request.setRelatedEntityId(partsId.toString());
        request.setRelatedEntityType("PARTS");
        request.setDeliveryChannels(determinePartsChannels(newStatus));
        return request;
    }

    // ==================== CHATBOT EVENTS ====================

    public static NotificationRequest createChatSessionStartedNotification(
            String customerId, String sessionId, String agentName) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Chat Session Started");
        request.setMessage(String.format("Welcome! Your chat session has started. How can we help you today?"));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.LOW);
        request.setRelatedEntityId(sessionId);
        request.setRelatedEntityType("CHAT_SESSION");
        request.setDeliveryChannels("[\"WEB\"]");
        return request;
    }

    public static NotificationRequest createChatSessionClosedNotification(
            String customerId, String sessionId, Integer duration, String agentName) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Chat Session Closed");
        request.setMessage(String.format("Thank you! Your chat session has ended. Session duration: %d minutes.",
                duration));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.LOW);
        request.setRelatedEntityId(sessionId);
        request.setRelatedEntityType("CHAT_SESSION");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
        return request;
    }

    public static NotificationRequest createCustomerEscalationRequestedNotification(
            String customerId, String sessionId, String reason, String urgency) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Escalation Request Received");
        request.setMessage(String.format("We've received your request for human assistance. A support agent will be with you shortly."));
        request.setType(NotificationType.GENERAL);
        request.setPriority(NotificationPriority.MEDIUM);
        request.setRelatedEntityId(sessionId);
        request.setRelatedEntityType("CHAT_SESSION");
        request.setDeliveryChannels("[\"WEB\"]");
        return request;
    }

    // ==================== TRACKING EVENTS ====================

    public static NotificationRequest createTaskCompletedNotification(
            String customerId, String taskId, String taskDescription, String completedBy) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(customerId);
        request.setTitle("Task Completed");
        request.setMessage(String.format("Task '%s' has been completed by %s",
                taskDescription, completedBy));
        request.setType(NotificationType.TASK_COMPLETED);
        request.setPriority(NotificationPriority.MEDIUM);
        request.setRelatedEntityId(taskId);
        request.setRelatedEntityType("TASK");
        request.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");
        return request;
    }

    public static NotificationRequest createTaskAssignedNotification(
            String userId, String taskId, String taskDescription, String dueDate) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle("New Task Assigned");
        request.setMessage(String.format("You have been assigned a new task: %s. Due date: %s",
                taskDescription, dueDate));
        request.setType(NotificationType.TASK_ASSIGNED);
        request.setPriority(NotificationPriority.HIGH);
        request.setRelatedEntityId(taskId);
        request.setRelatedEntityType("TASK");
        request.setDeliveryChannels("[\"WEB\", \"PUSH\"]");
        return request;
    }

    // ==================== HELPER METHODS ====================

    private static NotificationPriority determinePartsPriority(String status) {
        return switch (status.toUpperCase()) {
            case "OUT_OF_STOCK", "DISCONTINUED" -> NotificationPriority.HIGH;
            case "ORDERED", "IN_TRANSIT" -> NotificationPriority.MEDIUM;
            default -> NotificationPriority.LOW;
        };
    }

    private static NotificationType determinePartsType(String status) {
        return switch (status.toUpperCase()) {
            case "OUT_OF_STOCK", "DISCONTINUED" -> NotificationType.SYSTEM_ALERT;
            default -> NotificationType.GENERAL;
        };
    }

    private static String determinePartsChannels(String status) {
        return switch (status.toUpperCase()) {
            case "OUT_OF_STOCK", "DISCONTINUED" -> "[\"WEB\", \"EMAIL\", \"SMS\"]";
            case "ORDERED", "IN_TRANSIT", "AVAILABLE" -> "[\"WEB\", \"EMAIL\"]";
            default -> "[\"WEB\"]";
        };
    }
}
