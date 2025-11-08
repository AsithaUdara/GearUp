package com.gearup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(
    exclude = {
        org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
    },
    scanBasePackages = {"com.gearup"}
)
@EnableDiscoveryClient
public class PartsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartsServiceApplication.class, args);
    }
}