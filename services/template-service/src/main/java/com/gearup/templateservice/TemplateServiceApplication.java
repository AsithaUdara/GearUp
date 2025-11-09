package com.gearup.templateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.gearup.shared.messaging.RabbitMQConfig;

@SpringBootApplication
@Import(RabbitMQConfig.class)
public class TemplateServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TemplateServiceApplication.class, args);
    }
}
