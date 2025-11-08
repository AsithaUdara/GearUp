package com.gearup.analyticalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = "com.gearup")
@EnableJpaAuditing
public class AnalyticalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticalServiceApplication.class, args);
    }
}
