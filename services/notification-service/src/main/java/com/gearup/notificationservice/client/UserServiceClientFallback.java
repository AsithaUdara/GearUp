package com.gearup.notificationservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for UserServiceClient.
 * Provides resilience when user-auth-service is unavailable.
 * 
 * This follows the Circuit Breaker pattern to prevent cascading failures.
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);
    
    @Override
    public UserDetailsResponse getUserDetails(String userId) {
        log.warn("user-auth-service is unavailable. Returning fallback user details for userId: {}", userId);
        
        // Return a default/cached response
        UserDetailsResponse fallbackResponse = new UserDetailsResponse();
        fallbackResponse.setUserId(userId);
        fallbackResponse.setEmail("unavailable@gearup.com");
        fallbackResponse.setDisplayName("User Service Unavailable");
        fallbackResponse.setEmailVerified(false);
        
        return fallbackResponse;
    }
    
    @Override
    public Boolean userExists(String userId) {
        log.warn("user-auth-service is unavailable. Cannot verify user existence for userId: {}", userId);
        // Conservative approach: assume user exists to prevent blocking operations
        return true;
    }
}
