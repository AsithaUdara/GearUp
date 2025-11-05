package com.gearup.notificationservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.notificationservice.client.UserDetailsResponse;
import com.gearup.notificationservice.client.UserServiceClient;

/**
 * Test controller to demonstrate cross-service communication.
 * 
 * Shows how to use Feign clients to communicate with other microservices
 * via Eureka service discovery.
 */
@RestController
@RequestMapping("/api/cross-service-test")
public class CrossServiceTestController {
    
    private static final Logger log = LoggerFactory.getLogger(CrossServiceTestController.class);
    
    private final UserServiceClient userServiceClient;
    
    public CrossServiceTestController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }
    
    /**
     * Test endpoint to verify cross-service communication.
     * 
     * GET /api/cross-service-test/user/{userId}
     * 
     * This demonstrates:
     * 1. Synchronous REST call to user-auth-service via Feign
     * 2. Service discovery via Eureka
     * 3. Fallback handling when service is unavailable
     * 
     * @param userId User ID to fetch from user-auth-service
     * @return Combined response with user details and metadata
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> testCrossServiceCommunication(@PathVariable String userId) {
        log.info("Testing cross-service communication for userId: {}", userId);
        
        try {
            // Call user-auth-service via Feign client
            UserDetailsResponse userDetails = userServiceClient.getUserDetails(userId);
            Boolean userExists = userServiceClient.userExists(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully fetched user details from user-auth-service");
            response.put("userDetails", userDetails);
            response.put("userExists", userExists);
            response.put("communicationMethod", "Feign Client + Eureka Discovery");
            
            log.info("Successfully fetched user details for userId: {}", userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error communicating with user-auth-service: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error communicating with user-auth-service");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("note", "Fallback was triggered if configured");
            
            return ResponseEntity.status(503).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint to verify the controller is working.
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "notification-service");
        response.put("crossServiceCommunication", "enabled");
        
        return ResponseEntity.ok(response);
    }
}
