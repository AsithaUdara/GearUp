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
    
    @Bean
    public Binding modificationRequestCreatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(modificationExchange())
                .with(RabbitMQConstants.MODIFICATION_REQUEST_CREATED_KEY);
    }
    
    @Bean
    public Binding modificationRejectedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(modificationExchange())
                .with(RabbitMQConstants.MODIFICATION_REQUEST_REJECTED_KEY);
    }
    
    @Bean
    public Binding modificationCancelledBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(modificationExchange())
                .with(RabbitMQConstants.MODIFICATION_REQUEST_CANCELLED_KEY);
    }
    
    @Bean
    public Binding modificationStatusChangedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(modificationExchange())
                .with(RabbitMQConstants.MODIFICATION_REQUEST_STATUS_CHANGED_KEY);
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
    
    @Bean
    public Binding partsInventoryLowBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(partsExchange())
                .with(RabbitMQConstants.PARTS_INVENTORY_LOW_KEY);
    }
    
    @Bean
    public Binding partsStatusChangedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(partsExchange())
                .with(RabbitMQConstants.PARTS_STATUS_CHANGED_KEY);
    }
    
    // ===========================
    // Chatbot Exchange Bindings
    // ===========================
    
    @Bean
    public TopicExchange chatbotExchange() {
        return new TopicExchange(RabbitMQConstants.CHATBOT_EXCHANGE);
    }
    
    @Bean
    public Binding chatSessionStartedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(chatbotExchange())
                .with(RabbitMQConstants.CHAT_SESSION_STARTED_KEY);
    }
    
    @Bean
    public Binding chatSessionClosedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(chatbotExchange())
                .with(RabbitMQConstants.CHAT_SESSION_CLOSED_KEY);
    }
    
    @Bean
    public Binding customerEscalationRequestedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(chatbotExchange())
                .with(RabbitMQConstants.CUSTOMER_ESCALATION_REQUESTED_KEY);
    }
    
    // ===========================
    // User Exchange Bindings (Cross-Service)
    // ===========================
    
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(RabbitMQConstants.USER_EXCHANGE);
    }
    
    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(userExchange())
                .with(RabbitMQConstants.USER_REGISTERED_ROUTING_KEY);
    }
    
    @Bean
    public Binding roleAssignedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(userExchange())
                .with(RabbitMQConstants.ROLE_ASSIGNED_ROUTING_KEY);
    }
    
    // ===========================
    // Vehicle Exchange Bindings (Cross-Service)
    // ===========================
    
    @Bean
    public TopicExchange vehicleExchange() {
        return new TopicExchange(RabbitMQConstants.VEHICLE_EXCHANGE);
    }
    
    @Bean
    public Binding vehicleRegisteredBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(vehicleExchange())
                .with(RabbitMQConstants.VEHICLE_REGISTERED_ROUTING_KEY);
    }
    
    @Bean
    public Binding vehicleUpdatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(vehicleExchange())
                .with(RabbitMQConstants.VEHICLE_UPDATED_ROUTING_KEY);
    }
    
    // ===========================
    // Customer Exchange Bindings (Cross-Service)
    // ===========================
    
    @Bean
    public TopicExchange customerExchange() {
        return new TopicExchange(RabbitMQConstants.CUSTOMER_EXCHANGE);
    }
    
    @Bean
    public Binding customerRegisteredBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(customerExchange())
                .with(RabbitMQConstants.CUSTOMER_REGISTERED_ROUTING_KEY);
    }
    
    @Bean
    public Binding customerKycChangedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(customerExchange())
                .with(RabbitMQConstants.CUSTOMER_KYC_CHANGED_KEY);
    }
    
    @Bean
    public Binding invoicePaidBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(paymentExchange())
                .with(RabbitMQConstants.INVOICE_PAID_ROUTING_KEY);
    }
}
