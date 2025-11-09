package com.gearup.vehicleservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String VEHICLE_EXCHANGE = "vehicle.exchange";
    public static final String VEHICLE_QUEUE = "vehicle.queue";

    @Bean
    public TopicExchange vehicleExchange() {
        return new TopicExchange(VEHICLE_EXCHANGE);
    }

    @Bean
    public Queue vehicleQueue() {
        return new Queue(VEHICLE_QUEUE, true);
    }

    @Bean
    public Binding vehicleBinding() {
        return BindingBuilder.bind(vehicleQueue()).to(vehicleExchange()).with("vehicle.*");
    }
}
