package com.gearup.trackingservice.config;

import com.gearup.shared.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Tracking Service
 * Declares queues and bindings for event consumption
 * Note: MessageConverter and RabbitTemplate are provided by shared-libs/common-utils
 */
@Configuration
public class TrackingRabbitMQConfig {
    
    /**
     * Tracking queue for receiving appointment events
     */
    @Bean
    public Queue trackingQueue() {
        return new Queue(RabbitMQConstants.TRACKING_QUEUE, true);  // durable=true
    }
    
    /**
     * Bind tracking queue to appointment exchange with employee.assigned routing key
     * This ensures EmployeeAssignedToAppointmentEvent is delivered to tracking service
     */
    @Bean
    public Binding trackingQueueBinding(Queue trackingQueue, TopicExchange appointmentExchange) {
        return BindingBuilder
                .bind(trackingQueue)
                .to(appointmentExchange)
                .with(RabbitMQConstants.EMPLOYEE_ASSIGNED_KEY);
    }
}
