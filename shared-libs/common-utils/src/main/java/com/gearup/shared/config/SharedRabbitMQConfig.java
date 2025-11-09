package com.gearup.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gearup.shared.messaging.RabbitMQConstants;

/**
 * Shared RabbitMQ Configuration
 * This configuration is used by all microservices
 * Defines exchanges, queues, and bindings
 */
@Configuration
public class SharedRabbitMQConfig {

    /**
     * RabbitAdmin - CRITICAL for auto-declaration of exchanges, queues, and bindings
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * JSON Message Converter for serializing/deserializing events
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    // ==================== EXCHANGES ====================

    @Bean
    public TopicExchange appointmentExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.APPOINTMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange modificationExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.MODIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange trackingExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.TRACKING_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange paymentExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.PAYMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.USER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange customerExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.CUSTOMER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange partsExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.PARTS_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange vehicleExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.VEHICLE_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange analyticsExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.ANALYTICS_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== DEAD LETTER EXCHANGE ====================

    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstants.DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitMQConstants.DEAD_LETTER_ROUTING_KEY);
    }
}
