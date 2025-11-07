package com.gearup.analyticalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AnalyticalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticalServiceApplication.class, args);
    }
}
