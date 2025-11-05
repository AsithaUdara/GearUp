package com.gearup.notificationservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients (cross-service communication).
 * 
 * Feign provides declarative REST client capabilities with:
 * - Automatic service discovery via Eureka
 * - Load balancing
 * - Circuit breaker integration
 * - Request/response logging
 */
@Configuration
@EnableFeignClients(basePackages = "com.gearup.notificationservice.client")
public class FeignConfig {
    
    // Additional Feign configuration can be added here:
    // - Custom error decoders
    // - Request interceptors (for authentication headers)
    // - Logging levels
    // - Timeouts and retry policies
}
