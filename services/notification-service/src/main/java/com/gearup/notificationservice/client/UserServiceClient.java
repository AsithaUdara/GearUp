package com.gearup.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for cross-service communication with user-auth-service.
 * Demonstrates synchronous REST-based service-to-service communication via Eureka.
 * 
 * The @FeignClient annotation automatically discovers the service via Eureka Server
 * and handles load balancing across multiple instances.
 */
@FeignClient(
    name = "user-auth-service",  // Service name registered in Eureka
    fallback = UserServiceClientFallback.class  // Fallback for resilience
)
public interface UserServiceClient {
    
    /**
     * Get user details by user ID.
     * This endpoint would be exposed by the user-auth-service.
     * 
     * @param userId The user ID to fetch details for
     * @return User details response
     */
    @GetMapping("/api/users/{userId}")
    UserDetailsResponse getUserDetails(@PathVariable("userId") String userId);
    
    /**
     * Verify if a user exists.
     * 
     * @param userId The user ID to verify
     * @return Boolean indicating if user exists
     */
    @GetMapping("/api/users/{userId}/exists")
    Boolean userExists(@PathVariable("userId") String userId);
}
