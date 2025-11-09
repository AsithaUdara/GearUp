package com.gearup.shared.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import java.util.Objects;

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
        // Ensure non-null for static analysis
        return new RabbitAdmin(Objects.requireNonNull(connectionFactory, "connectionFactory"));
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
        // Use a simple durable queue without TTL to avoid precondition mismatches
        // if the queue was created previously without the TTL argument.
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    /**
     * Queue for user-auth to consume template events
     */
    @Bean
    public Queue templateQueue() {
        // Keep declaration minimal to be compatible with existing queues (no TTL arg)
        return QueueBuilder.durable(TEMPLATE_QUEUE).build();
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
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // Trust event model package for type mapping to prevent conversion failures
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.gearup.shared.event");
        converter.setClassMapper(typeMapper);
        return converter;
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(Objects.requireNonNull(connectionFactory, "connectionFactory"));
        rabbitTemplate.setMessageConverter(Objects.requireNonNull(jsonMessageConverter(), "messageConverter"));
        return rabbitTemplate;
    }

    /**
     * Listener container factory so that @RabbitListener annotated methods use the JSON
     * converter instead of the default SimpleMessageConverter (which only gives byte[]).
     * Also sets missingQueuesFatal to false to avoid startup aborts while declarations race.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setMissingQueuesFatal(false);
        // Optionally tune concurrency later if needed
        return factory;
    }
}
