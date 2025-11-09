package com.gearup.notificationservice.config;

import com.gearup.shared.messaging.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for notification service
 * Consumes events from multiple exchanges to send notifications
 */
@Configuration
public class RabbitMQConfig {
    
    // ===========================
    // Message Converter
    // ===========================
    
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }
    
    // ===========================
    // Dead Letter Queue
    // ===========================
    
    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(RabbitMQConstants.NOTIFICATION_DLQ)
                .build();
    }
    
    @Bean
    public DirectExchange notificationDlxExchange() {
        return new DirectExchange(RabbitMQConstants.NOTIFICATION_DLX_EXCHANGE);
    }
    
    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder
                .bind(notificationDlq())
                .to(notificationDlxExchange())
                .with(RabbitMQConstants.NOTIFICATION_DLQ);
    }
    
    // ===========================
    // Notification Queue
    // ===========================
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(RabbitMQConstants.NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.NOTIFICATION_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.NOTIFICATION_DLQ)
                .build();
    }
    
    // ===========================
    // Appointment Exchange Bindings
    // ===========================
    
    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(RabbitMQConstants.APPOINTMENT_EXCHANGE);
    }
    
    @Bean
    public Binding appointmentCreatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(appointmentExchange())
                .with(RabbitMQConstants.APPOINTMENT_CREATED_ROUTING_KEY);
    }
    
    @Bean
    public Binding appointmentApprovedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(appointmentExchange())
                .with(RabbitMQConstants.APPOINTMENT_APPROVED_ROUTING_KEY);
    }
    
    @Bean
    public Binding appointmentCancelledBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(appointmentExchange())
                .with(RabbitMQConstants.APPOINTMENT_CANCELLED_ROUTING_KEY);
    }
    
    @Bean
    public Binding employeeAssignedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(appointmentExchange())
                .with(RabbitMQConstants.EMPLOYEE_ASSIGNED_TO_APPOINTMENT_ROUTING_KEY);
    }
    
    // ===========================
    // Payment Exchange Bindings
    // ===========================
    
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(RabbitMQConstants.PAYMENT_EXCHANGE);
    }
    
    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(paymentExchange())
                .with(RabbitMQConstants.PAYMENT_COMPLETED_ROUTING_KEY);
    }
    
    @Bean
    public Binding invoiceCreatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(paymentExchange())
                .with(RabbitMQConstants.INVOICE_CREATED_ROUTING_KEY);
    }
    
    // ===========================
    // Tracking Exchange Bindings
    // ===========================
    
    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange(RabbitMQConstants.TRACKING_EXCHANGE);
    }
    
    @Bean
    public Binding taskCompletedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(trackingExchange())
                .with(RabbitMQConstants.TASK_COMPLETED_ROUTING_KEY);
    }
    
    @Bean
    public Binding issueReportedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(trackingExchange())
                .with(RabbitMQConstants.ISSUE_REPORTED_ROUTING_KEY);
    }
    
    // ===========================
    // Modification Exchange Bindings
    // ===========================
    
    @Bean
    public TopicExchange modificationExchange() {
        return new TopicExchange(RabbitMQConstants.MODIFICATION_EXCHANGE);
    }
    
    @Bean
    public Binding modificationApprovedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(modificationExchange())
                .with(RabbitMQConstants.MODIFICATION_APPROVED_ROUTING_KEY);
    }
    
    @Bean
    public Binding modificationCompletedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(modificationExchange())
                .with(RabbitMQConstants.MODIFICATION_COMPLETED_ROUTING_KEY);
    }
    
    // ===========================
    // Parts Exchange Bindings
    // ===========================
    
    @Bean
    public TopicExchange partsExchange() {
        return new TopicExchange(RabbitMQConstants.PARTS_EXCHANGE);
    }
    
    @Bean
    public Binding lowStockAlertBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(partsExchange())
                .with(RabbitMQConstants.LOW_STOCK_ALERT_ROUTING_KEY);
    }
}
