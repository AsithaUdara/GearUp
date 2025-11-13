package com.gearup.modificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.gearup")
public class ModificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModificationServiceApplication.class, args);
    }
}