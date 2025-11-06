package com.gearup.customerservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String CUSTOMER_EXCHANGE = "customer.exchange";
    public static final String CUSTOMER_QUEUE = "customer.queue";

    @Bean
    public TopicExchange customerExchange() { return new TopicExchange(CUSTOMER_EXCHANGE); }

    @Bean
    public Queue customerQueue() { return new Queue(CUSTOMER_QUEUE, true); }

    @Bean
    public Binding customerBinding() {
        return BindingBuilder.bind(customerQueue()).to(customerExchange()).with("customer.*");
    }
}
