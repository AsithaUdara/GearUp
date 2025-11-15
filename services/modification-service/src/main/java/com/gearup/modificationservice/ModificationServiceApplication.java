package com.gearup.modificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ModificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModificationServiceApplication.class, args);
    }
}
