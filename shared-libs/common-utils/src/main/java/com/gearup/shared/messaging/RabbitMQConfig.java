package com.gearup.shared.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange name
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    // Exchange for service template domain events
    public static final String TEMPLATE_EXCHANGE = "template.exchange";
    
    // Queue names
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    // Dedicated queue for user-auth-service to consume template events
    public static final String TEMPLATE_QUEUE = "template.user-auth.queue";
    
    // Routing keys
    public static final String INVOICE_CREATED_KEY = "invoice.created";
    public static final String INVOICE_UPDATED_KEY = "invoice.updated";
    public static final String INVOICE_PAID_KEY = "invoice.paid";
    public static final String TASK_ASSIGNED_KEY = "task.assigned";
    public static final String TASK_COMPLETED_KEY = "task.completed";
    // Routing keys for template lifecycle
    public static final String TEMPLATE_CREATED_KEY = "template.created";
    public static final String TEMPLATE_UPDATED_KEY = "template.updated";
    public static final String TEMPLATE_DELETED_KEY = "template.deleted";

    /**
     * RabbitAdmin - CRITICAL for auto-declaration of exchanges, queues, and bindings
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * Create Topic Exchange for routing messages based on routing keys
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    /**
     * Topic exchange for service template events
     */
    @Bean
    public TopicExchange templateExchange() {
        return new TopicExchange(TEMPLATE_EXCHANGE);
    }

    /**
     * Create notification queue
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 hours TTL
                .build();
    }

    /**
     * Queue for user-auth to consume template events
     */
    @Bean
    public Queue templateQueue() {
        return QueueBuilder.durable(TEMPLATE_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    /**
     * Bind queue to exchange with routing patterns
     */
    @Bean
    public Binding invoiceCreatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(INVOICE_CREATED_KEY);
    }

    @Bean
    public Binding invoiceUpdatedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(INVOICE_UPDATED_KEY);
    }

    @Bean
    public Binding invoicePaidBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(INVOICE_PAID_KEY);
    }

    @Bean
    public Binding taskAssignedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(TASK_ASSIGNED_KEY);
    }

    @Bean
    public Binding taskCompletedBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(TASK_COMPLETED_KEY);
    }

    @Bean
    public Binding templateCreatedBinding() {
        return BindingBuilder
                .bind(templateQueue())
                .to(templateExchange())
                .with(TEMPLATE_CREATED_KEY);
    }

    @Bean
    public Binding templateUpdatedBinding() {
        return BindingBuilder
                .bind(templateQueue())
                .to(templateExchange())
                .with(TEMPLATE_UPDATED_KEY);
    }

    @Bean
    public Binding templateDeletedBinding() {
        return BindingBuilder
                .bind(templateQueue())
                .to(templateExchange())
                .with(TEMPLATE_DELETED_KEY);
    }

    /**
     * JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
