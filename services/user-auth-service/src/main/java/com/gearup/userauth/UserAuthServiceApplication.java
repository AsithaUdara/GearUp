package com.gearup.userauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.gearup.shared.messaging.RabbitMQConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {
    "com.gearup.userauth",
    "com.gearup.security",   // shared security lib
    "com.gearup.shared"      // include shared messaging config (RabbitMQConfig) and other shared beans
})
@EnableJpaRepositories(basePackages = "com.gearup.userauth.repository")
@Import(RabbitMQConfig.class)
public class UserAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
