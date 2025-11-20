package com.gearup.analyticalservice.config;

import com.gearup.shared.messaging.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for analytical service
 * Binds analytics queue to ALL exchanges to collect comprehensive analytics data
 * Note: MessageConverter and RabbitTemplate are provided by shared-libs/common-utils
 */
@Configuration
public class AnalyticalRabbitMQConfig {
    
    // ===========================
    // Listener Container Factory
    // ===========================
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(20);
        return factory;
    }
    
    // ===========================
    // Analytics Queue (Central collection point)
    // ===========================
    
    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(RabbitMQConstants.ANALYTICS_QUEUE)
                .build();
    }
    
    // ===========================
    // Topic Exchanges (Declare to ensure they exist)
    // ===========================
    
    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(RabbitMQConstants.APPOINTMENT_EXCHANGE);
    }
    
    @Bean
    public TopicExchange modificationExchange() {
        return new TopicExchange(RabbitMQConstants.MODIFICATION_EXCHANGE);
    }
    
    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange(RabbitMQConstants.TRACKING_EXCHANGE);
    }
    
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(RabbitMQConstants.PAYMENT_EXCHANGE);
    }
    
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(RabbitMQConstants.USER_EXCHANGE);
    }
    
    @Bean
    public TopicExchange vehicleExchange() {
        return new TopicExchange(RabbitMQConstants.VEHICLE_EXCHANGE);
    }
    
    @Bean
    public TopicExchange partsExchange() {
        return new TopicExchange(RabbitMQConstants.PARTS_EXCHANGE);
    }
    
    @Bean
    public TopicExchange customerExchange() {
        return new TopicExchange(RabbitMQConstants.CUSTOMER_EXCHANGE);
    }
    
    // ===========================
    // Bindings - Analytics Queue listens to ALL events
    // ===========================
    
    // Appointment Events
    @Bean
    public Binding analyticsAppointmentBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(appointmentExchange())
                .with("appointment.#");  // All appointment events
    }
    
    // Modification Events
    @Bean
    public Binding analyticsModificationBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(modificationExchange())
                .with("modification.#");  // All modification events
    }
    
    // Tracking Events
    @Bean
    public Binding analyticsTrackingBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(trackingExchange())
                .with("tracking.#");  // All tracking events
    }
    
    // Payment Events
    @Bean
    public Binding analyticsPaymentBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(paymentExchange())
                .with("payment.#");  // All payment events
    }
    
    // User Events
    @Bean
    public Binding analyticsUserBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(userExchange())
                .with("user.#");  // All user events
    }
    
    // Vehicle Events
    @Bean
    public Binding analyticsVehicleBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(vehicleExchange())
                .with("vehicle.#");  // All vehicle events
    }
    
    // Parts Events
    @Bean
    public Binding analyticsPartsBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(partsExchange())
                .with("parts.#");  // All parts events
    }
    
    // Customer Events
    @Bean
    public Binding analyticsCustomerBinding() {
        return BindingBuilder
                .bind(analyticsQueue())
                .to(customerExchange())
                .with("customer.#");  // All customer events
    }
}
