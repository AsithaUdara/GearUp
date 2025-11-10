package com.gearup.paymentservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gearup.shared.event.payment.*;
import com.gearup.shared.messaging.EventPublisher;
import com.gearup.shared.messaging.RabbitMQConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event Publisher Service for Payment Service
 * Publishes payment-related events to RabbitMQ for inter-service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final EventPublisher eventPublisher;

    /**
     * Publish invoice created event
     */
    public void publishInvoiceCreatedEvent(String invoiceId, String invoiceNumber, String userId, Double amount, String customerName) {
        try {
            InvoiceCreatedEvent event = new InvoiceCreatedEvent(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now(),
                invoiceId,
                invoiceNumber,
                amount,
                customerName
            );
            
            eventPublisher.publish(
                RabbitMQConstants.PAYMENT_EXCHANGE,
                RabbitMQConstants.INVOICE_CREATED_KEY,
                event
            );
            
            log.info("📢 Published InvoiceCreatedEvent for invoice: {}", invoiceId);
        } catch (Exception e) {
            log.error("❌ Failed to publish InvoiceCreatedEvent for invoice: {}", invoiceId, e);
        }
    }

    /**
     * Publish invoice paid event
     */
    public void publishInvoicePaidEvent(String invoiceId, String userId, Double amount, String paymentMethod) {
        try {
            InvoicePaidEvent event = new InvoicePaidEvent(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now(),
                invoiceId,
                amount,
                paymentMethod
            );
            
            eventPublisher.publish(
                RabbitMQConstants.PAYMENT_EXCHANGE,
                RabbitMQConstants.INVOICE_PAID_KEY,
                event
            );
            
            log.info("📢 Published InvoicePaidEvent for invoice: {}", invoiceId);
        } catch (Exception e) {
            log.error("❌ Failed to publish InvoicePaidEvent for invoice: {}", invoiceId, e);
        }
    }

    /**
     * Publish payment completed event
     */
    public void publishPaymentCompletedEvent(String paymentId, String userId, Double amount, String transactionId, String paymentMethod) {
        try {
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now(),
                paymentId,
                transactionId,
                amount,
                paymentMethod,
                "COMPLETED"
            );
            
            eventPublisher.publish(
                RabbitMQConstants.PAYMENT_EXCHANGE,
                RabbitMQConstants.PAYMENT_COMPLETED_KEY,
                event
            );
            
            log.info("📢 Published PaymentCompletedEvent for payment: {}", paymentId);
        } catch (Exception e) {
            log.error("❌ Failed to publish PaymentCompletedEvent for payment: {}", paymentId, e);
        }
    }

    /**
     * Publish invoice updated event
     */
    public void publishInvoiceUpdatedEvent(String invoiceId, String userId, Double newAmount) {
        try {
            InvoiceUpdatedEvent event = new InvoiceUpdatedEvent(
                UUID.randomUUID().toString(),
                userId,
                LocalDateTime.now(),
                invoiceId,
                newAmount,
                LocalDateTime.now()
            );
            
            eventPublisher.publish(
                RabbitMQConstants.PAYMENT_EXCHANGE,
                RabbitMQConstants.INVOICE_UPDATED_KEY,
                event
            );
            
            log.info("📢 Published InvoiceUpdatedEvent for invoice: {}", invoiceId);
        } catch (Exception e) {
            log.error("❌ Failed to publish InvoiceUpdatedEvent for invoice: {}", invoiceId, e);
        }
    }
}
