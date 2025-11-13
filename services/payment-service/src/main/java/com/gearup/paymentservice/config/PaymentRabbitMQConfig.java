package com.gearup.paymentservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Payment Service
 * Defines queues and bindings for payment-related events
 * Note: Exchanges are defined in shared-libs/common-utils SharedRabbitMQConfig
 */
@Configuration
public class PaymentRabbitMQConfig {

    // Exchange names
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Queue names
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String PAYMENT_REQUEST_QUEUE = "payment.request.queue";
    public static final String PAYMENT_BILLING_QUEUE = "payment.billing.queue";

    // Routing keys
    public static final String PAYMENT_CREATED_KEY = "payment.created";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_KEY = "payment.failed";
    public static final String PAYMENT_REQUEST_CREATED_KEY = "payment.request.created";
    public static final String PAYMENT_REQUEST_APPROVED_KEY = "payment.request.approved";
    public static final String PAYMENT_REQUEST_REJECTED_KEY = "payment.request.rejected";
    public static final String BILL_CREATED_KEY = "bill.created";
    public static final String BILL_PAID_KEY = "bill.paid";

    /**
     * Payment Queue - Receives all payment-related events
     */
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder
                .durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_EXCHANGE + ".dlx")
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .build();
    }

    /**
     * Payment Request Queue - Handles payment request events
     */
    @Bean
    public Queue paymentRequestQueue() {
        return QueueBuilder
                .durable(PAYMENT_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_EXCHANGE + ".dlx")
                .build();
    }

    /**
     * Billing Queue - Handles billing-related events
     */
    @Bean
    public Queue paymentBillingQueue() {
        return QueueBuilder
                .durable(PAYMENT_BILLING_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_EXCHANGE + ".dlx")
                .build();
    }

    /**
     * Dead Letter Queue for failed messages
     */
    @Bean
    public Queue paymentDeadLetterQueue() {
        return QueueBuilder
                .durable(PAYMENT_EXCHANGE + ".dlq")
                .build();
    }

    /**
     * Dead Letter Exchange
     */
    @Bean
    public DirectExchange paymentDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(PAYMENT_EXCHANGE + ".dlx")
                .durable(true)
                .build();
    }

    /**
     * Binding: Payment Queue to Payment Exchange
     */
    @Bean
    public Binding paymentQueueBinding(@Qualifier("paymentExchange") TopicExchange paymentExchange) {
        return BindingBuilder
                .bind(paymentQueue())
                .to(paymentExchange)
                .with("payment.*");
    }

    /**
     * Binding: Payment Request Queue to Payment Exchange
     */
    @Bean
    public Binding paymentRequestQueueBinding(@Qualifier("paymentExchange") TopicExchange paymentExchange) {
        return BindingBuilder
                .bind(paymentRequestQueue())
                .to(paymentExchange)
                .with("payment.request.*");
    }

    /**
     * Binding: Billing Queue to Payment Exchange
     */
    @Bean
    public Binding paymentBillingQueueBinding(@Qualifier("paymentExchange") TopicExchange paymentExchange) {
        return BindingBuilder
                .bind(paymentBillingQueue())
                .to(paymentExchange)
                .with("bill.*");
    }

    /**
     * Binding: Dead Letter Queue to Dead Letter Exchange
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(paymentDeadLetterQueue())
                .to(paymentDeadLetterExchange())
                .with("#");
    }
}
