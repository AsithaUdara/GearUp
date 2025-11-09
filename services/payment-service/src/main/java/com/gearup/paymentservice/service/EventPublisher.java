package com.gearup.paymentservice.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.gearup.paymentservice.config.PaymentRabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event Publisher Service for Payment Service
 * Publishes payment-related events to RabbitMQ for inter-service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish payment created event
     */
    public void publishPaymentCreatedEvent(Long paymentId, String userId, Double amount) {
        Map<String, Object> event = createBaseEvent("PAYMENT_CREATED");
        event.put("paymentId", paymentId);
        event.put("userId", userId);
        event.put("amount", amount);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.PAYMENT_CREATED_KEY, 
                    event);
    }

    /**
     * Publish payment completed event
     */
    public void publishPaymentCompletedEvent(Long paymentId, String userId, Double amount, String transactionId) {
        Map<String, Object> event = createBaseEvent("PAYMENT_COMPLETED");
        event.put("paymentId", paymentId);
        event.put("userId", userId);
        event.put("amount", amount);
        event.put("transactionId", transactionId);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.PAYMENT_COMPLETED_KEY, 
                    event);
        
        // Also publish to notification exchange for user notification
        publishToNotificationService(userId, "Payment Completed", 
            String.format("Your payment of $%.2f has been completed successfully.", amount));
    }

    /**
     * Publish payment failed event
     */
    public void publishPaymentFailedEvent(Long paymentId, String userId, Double amount, String reason) {
        Map<String, Object> event = createBaseEvent("PAYMENT_FAILED");
        event.put("paymentId", paymentId);
        event.put("userId", userId);
        event.put("amount", amount);
        event.put("reason", reason);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.PAYMENT_FAILED_KEY, 
                    event);
        
        // Notify user about failed payment
        publishToNotificationService(userId, "Payment Failed", 
            String.format("Your payment of $%.2f has failed. Reason: %s", amount, reason));
    }

    /**
     * Publish payment request created event
     */
    public void publishPaymentRequestCreatedEvent(Long requestId, String userId, Double amount, String description) {
        Map<String, Object> event = createBaseEvent("PAYMENT_REQUEST_CREATED");
        event.put("requestId", requestId);
        event.put("userId", userId);
        event.put("amount", amount);
        event.put("description", description);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.PAYMENT_REQUEST_CREATED_KEY, 
                    event);
        
        // Notify user about new payment request
        publishToNotificationService(userId, "Payment Request Created", 
            String.format("A new payment request of $%.2f has been created.", amount));
    }

    /**
     * Publish payment request approved event
     */
    public void publishPaymentRequestApprovedEvent(Long requestId, String userId, String approvedBy) {
        Map<String, Object> event = createBaseEvent("PAYMENT_REQUEST_APPROVED");
        event.put("requestId", requestId);
        event.put("userId", userId);
        event.put("approvedBy", approvedBy);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.PAYMENT_REQUEST_APPROVED_KEY, 
                    event);
        
        publishToNotificationService(userId, "Payment Request Approved", 
            "Your payment request has been approved.");
    }

    /**
     * Publish payment request rejected event
     */
    public void publishPaymentRequestRejectedEvent(Long requestId, String userId, String reason) {
        Map<String, Object> event = createBaseEvent("PAYMENT_REQUEST_REJECTED");
        event.put("requestId", requestId);
        event.put("userId", userId);
        event.put("reason", reason);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.PAYMENT_REQUEST_REJECTED_KEY, 
                    event);
        
        publishToNotificationService(userId, "Payment Request Rejected", 
            String.format("Your payment request has been rejected. Reason: %s", reason));
    }

    /**
     * Publish bill created event
     */
    public void publishBillCreatedEvent(Long billId, String customerId, Double totalAmount) {
        Map<String, Object> event = createBaseEvent("BILL_CREATED");
        event.put("billId", billId);
        event.put("customerId", customerId);
        event.put("totalAmount", totalAmount);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.BILL_CREATED_KEY, 
                    event);
        
        publishToNotificationService(customerId, "New Bill Generated", 
            String.format("A new bill of $%.2f has been generated for your account.", totalAmount));
    }

    /**
     * Publish bill paid event
     */
    public void publishBillPaidEvent(Long billId, String customerId, Double amountPaid, String paymentMethod) {
        Map<String, Object> event = createBaseEvent("BILL_PAID");
        event.put("billId", billId);
        event.put("customerId", customerId);
        event.put("amountPaid", amountPaid);
        event.put("paymentMethod", paymentMethod);
        
        publishEvent(PaymentRabbitMQConfig.PAYMENT_EXCHANGE, 
                    PaymentRabbitMQConfig.BILL_PAID_KEY, 
                    event);
        
        publishToNotificationService(customerId, "Bill Payment Received", 
            String.format("Your payment of $%.2f has been received. Thank you!", amountPaid));
    }

    /**
     * Publish notification to notification service
     */
    private void publishToNotificationService(String userId, String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", "PAYMENT");
        notification.put("timestamp", LocalDateTime.now().toString());
        
        publishEvent(PaymentRabbitMQConfig.NOTIFICATION_EXCHANGE, "notification.created", notification);
    }

    /**
     * Create base event with common fields
     */
    private Map<String, Object> createBaseEvent(String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("source", "payment-service");
        return event;
    }

    /**
     * Publish event to RabbitMQ
     */
    private void publishEvent(String exchange, String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published event to exchange: {}, routing key: {}, event: {}", 
                    exchange, routingKey, event);
        } catch (AmqpException e) {
            log.error("Failed to publish event to exchange: {}, routing key: {}", 
                    exchange, routingKey, e);
        }
    }
}
