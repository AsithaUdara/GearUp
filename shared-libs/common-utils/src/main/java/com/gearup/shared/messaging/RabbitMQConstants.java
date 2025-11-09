package com.gearup.shared.messaging;

/**
 * Centralized RabbitMQ Constants for Event-Driven Architecture
 * Contains all exchanges, queues, and routing keys used across GearUp microservices
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {
        // Prevent instantiation
    }

    // ==================== EXCHANGES ====================
    
    // Main topic exchanges per service domain
    public static final String APPOINTMENT_EXCHANGE = "appointment.exchange";
    public static final String MODIFICATION_EXCHANGE = "modification.exchange";
    public static final String TRACKING_EXCHANGE = "tracking.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String CUSTOMER_EXCHANGE = "customer.exchange";
    public static final String PARTS_EXCHANGE = "parts.exchange";
    public static final String VEHICLE_EXCHANGE = "vehicle.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String ANALYTICS_EXCHANGE = "analytics.exchange";
    public static final String CHATBOT_EXCHANGE = "chatbot.exchange";

    // ==================== QUEUES ====================
    
    // Notification Service Queues (consumes from all services)
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    
    // Analytics Service Queues (consumes from all services)
    public static final String ANALYTICS_QUEUE = "analytics.queue";
    
    // Service-specific queues
    public static final String APPOINTMENT_QUEUE = "appointment.queue";
    public static final String MODIFICATION_QUEUE = "modification.queue";
    public static final String TRACKING_QUEUE = "tracking.queue";
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String USER_QUEUE = "user.queue";
    public static final String CUSTOMER_QUEUE = "customer.queue";
    public static final String PARTS_QUEUE = "parts.queue";
    public static final String VEHICLE_QUEUE = "vehicle.queue";

    // ==================== ROUTING KEYS ====================
    
    // APPOINTMENT SERVICE ROUTING KEYS
    public static final String APPOINTMENT_CREATED_KEY = "appointment.created";
    public static final String APPOINTMENT_CONFIRMED_KEY = "appointment.confirmed";
    public static final String APPOINTMENT_CANCELLED_KEY = "appointment.cancelled";
    public static final String APPOINTMENT_UPDATED_KEY = "appointment.updated";
    public static final String APPOINTMENT_APPROVED_KEY = "appointment.approved";
    public static final String EMPLOYEE_ASSIGNED_KEY = "appointment.employee.assigned";
    public static final String TIMESLOT_BLOCKED_KEY = "appointment.timeslot.blocked";
    public static final String TIMESLOT_UNBLOCKED_KEY = "appointment.timeslot.unblocked";

    // MODIFICATION SERVICE ROUTING KEYS
    public static final String MODIFICATION_REQUEST_CREATED_KEY = "modification.request.created";
    public static final String MODIFICATION_REQUEST_APPROVED_KEY = "modification.request.approved";
    public static final String MODIFICATION_REQUEST_REJECTED_KEY = "modification.request.rejected";
    public static final String MODIFICATION_REQUEST_STATUS_CHANGED_KEY = "modification.request.status.changed";
    public static final String MODIFICATION_REQUEST_COMPLETED_KEY = "modification.request.completed";
    public static final String MODIFICATION_REQUEST_CANCELLED_KEY = "modification.request.cancelled";

    // TRACKING SERVICE ROUTING KEYS
    public static final String TASK_CREATED_KEY = "tracking.task.created";
    public static final String TASK_ASSIGNED_KEY = "tracking.task.assigned";
    public static final String TASK_STARTED_KEY = "tracking.task.started";
    public static final String TASK_PROGRESS_UPDATED_KEY = "tracking.task.progress.updated";
    public static final String TASK_COMPLETED_KEY = "tracking.task.completed";
    public static final String TASK_BLOCKED_KEY = "tracking.task.blocked";
    public static final String PARTS_REQUEST_CREATED_KEY = "tracking.parts.request.created";
    public static final String PARTS_REQUEST_APPROVED_KEY = "tracking.parts.request.approved";
    public static final String PARTS_REQUEST_FULFILLED_KEY = "tracking.parts.request.fulfilled";
    public static final String SERVICE_PROGRESS_STARTED_KEY = "tracking.service.progress.started";
    public static final String SERVICE_PROGRESS_COMPLETED_KEY = "tracking.service.progress.completed";

    // PAYMENT SERVICE ROUTING KEYS
    public static final String PAYMENT_CREATED_KEY = "payment.created";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_KEY = "payment.failed";
    public static final String PAYMENT_REQUEST_CREATED_KEY = "payment.request.created";
    public static final String PAYMENT_REQUEST_APPROVED_KEY = "payment.request.approved";
    public static final String PAYMENT_REQUEST_REJECTED_KEY = "payment.request.rejected";
    public static final String INVOICE_CREATED_KEY = "payment.invoice.created";
    public static final String INVOICE_UPDATED_KEY = "payment.invoice.updated";
    public static final String INVOICE_PAID_KEY = "payment.invoice.paid";
    public static final String BILL_CREATED_KEY = "payment.bill.created";
    public static final String BILL_PAID_KEY = "payment.bill.paid";
    public static final String REVIEW_SUBMITTED_KEY = "payment.review.submitted";

    // USER-AUTH SERVICE ROUTING KEYS
    public static final String USER_REGISTERED_KEY = "user.registered";
    public static final String USER_UPDATED_KEY = "user.updated";
    public static final String ROLE_ASSIGNED_KEY = "user.role.assigned";
    public static final String USER_DEACTIVATED_KEY = "user.deactivated";
    public static final String PASSWORD_CHANGED_KEY = "user.password.changed";

    // CUSTOMER SERVICE ROUTING KEYS
    public static final String CUSTOMER_CREATED_KEY = "customer.created";
    public static final String CUSTOMER_UPDATED_KEY = "customer.updated";

    // PARTS SERVICE ROUTING KEYS
    public static final String PARTS_STATUS_CHANGED_KEY = "parts.status.changed";
    public static final String PARTS_INVENTORY_LOW_KEY = "parts.inventory.low";

    // VEHICLE SERVICE ROUTING KEYS
    public static final String VEHICLE_CREATED_KEY = "vehicle.created";
    public static final String VEHICLE_UPDATED_KEY = "vehicle.updated";
    public static final String VEHICLE_STATUS_CHANGED_KEY = "vehicle.status.changed";
    public static final String VEHICLE_MAINTENANCE_DUE_KEY = "vehicle.maintenance.due";

    // NOTIFICATION SERVICE ROUTING KEYS
    public static final String NOTIFICATION_SENT_KEY = "notification.sent";
    public static final String NOTIFICATION_FAILED_KEY = "notification.failed";

    // ANALYTICS SERVICE ROUTING KEYS
    public static final String REPORT_GENERATED_KEY = "analytics.report.generated";

    // CHATBOT SERVICE ROUTING KEYS
    public static final String CHAT_SESSION_STARTED_KEY = "chatbot.session.started";
    public static final String CHAT_SESSION_CLOSED_KEY = "chatbot.session.closed";
    public static final String CUSTOMER_ESCALATION_REQUESTED_KEY = "chatbot.escalation.requested";

    // ==================== ROUTING PATTERNS ====================
    
    // Patterns for binding queues to exchanges
    public static final String ALL_APPOINTMENT_EVENTS = "appointment.*";
    public static final String ALL_MODIFICATION_EVENTS = "modification.*";
    public static final String ALL_TRACKING_EVENTS = "tracking.*";
    public static final String ALL_PAYMENT_EVENTS = "payment.*";
    public static final String ALL_USER_EVENTS = "user.*";
    public static final String ALL_CUSTOMER_EVENTS = "customer.*";
    public static final String ALL_PARTS_EVENTS = "parts.*";
    public static final String ALL_VEHICLE_EVENTS = "vehicle.*";
    public static final String ALL_NOTIFICATION_EVENTS = "notification.*";
    
    // Cross-service patterns for notification service
    public static final String ALL_CREATED_EVENTS = "*.created";
    public static final String ALL_COMPLETED_EVENTS = "*.completed";
    public static final String ALL_CANCELLED_EVENTS = "*.cancelled";
    public static final String ALL_STATUS_CHANGED_EVENTS = "*.status.changed";

    // ==================== MESSAGE PROPERTIES ====================
    
    // Default message TTL (24 hours in milliseconds)
    public static final long DEFAULT_MESSAGE_TTL = 86400000L;
    
    // Priority levels
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_NORMAL = 5;
    public static final int PRIORITY_HIGH = 10;

    // ==================== DEAD LETTER EXCHANGE ====================
    
    public static final String DEAD_LETTER_EXCHANGE = "dlx.exchange";
    public static final String DEAD_LETTER_QUEUE = "dlx.queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dlx.#";
    
    // Service-specific DLX
    public static final String NOTIFICATION_DLX_EXCHANGE = "notification.dlx.exchange";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    
    // ==================== FULL ROUTING KEYS (with exchange prefix) ====================
    // Used for listener bindings
    
    public static final String APPOINTMENT_CREATED_ROUTING_KEY = APPOINTMENT_CREATED_KEY;
    public static final String APPOINTMENT_APPROVED_ROUTING_KEY = APPOINTMENT_APPROVED_KEY;
    public static final String APPOINTMENT_CANCELLED_ROUTING_KEY = APPOINTMENT_CANCELLED_KEY;
    public static final String EMPLOYEE_ASSIGNED_TO_APPOINTMENT_ROUTING_KEY = EMPLOYEE_ASSIGNED_KEY;
    
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = PAYMENT_COMPLETED_KEY;
    public static final String INVOICE_CREATED_ROUTING_KEY = INVOICE_CREATED_KEY;
    
    public static final String TASK_COMPLETED_ROUTING_KEY = TASK_COMPLETED_KEY;
    public static final String ISSUE_REPORTED_ROUTING_KEY = "tracking.issue.reported";
    
    public static final String MODIFICATION_APPROVED_ROUTING_KEY = MODIFICATION_REQUEST_APPROVED_KEY;
    public static final String MODIFICATION_COMPLETED_ROUTING_KEY = MODIFICATION_REQUEST_COMPLETED_KEY;
    
    public static final String LOW_STOCK_ALERT_ROUTING_KEY = PARTS_INVENTORY_LOW_KEY;
}
