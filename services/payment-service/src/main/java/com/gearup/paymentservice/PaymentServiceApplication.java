package com.gearup.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
    basePackages = "com.gearup",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASPECTJ,
            pattern = "com.gearup.shared.config.RedisConfig"
        ),
        @ComponentScan.Filter(
            type = FilterType.ASPECTJ,
            pattern = "com.gearup.shared.messaging.RabbitMQConfig"
        )
    }
)
public class PaymentServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
