package com.gearup.userauth.service;

import com.gearup.shared.event.user.*;
import com.gearup.shared.messaging.EventPublisher;
import com.gearup.shared.messaging.RabbitMQConstants;
import com.gearup.userauth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for publishing user-related events to RabbitMQ.
 * These events can be consumed by other microservices like notification, analytics, etc.
 */
@Service
public class UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(UserEventPublisher.class);
    
    private final EventPublisher eventPublisher;

    public UserEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes UserRegisteredEvent when a new user registers
     */
    public void publishUserRegisteredEvent(User user) {
        try {
            String primaryRole = user.getRoles().stream()
                .findFirst()
                .map(role -> role.getName())
                .orElse("USER");
                
            UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID().toString(),
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                primaryRole,
                LocalDateTime.now(),
                "FIREBASE"  // registrationMethod
            );
            
            eventPublisher.publish(
                RabbitMQConstants.USER_EXCHANGE,
                RabbitMQConstants.USER_REGISTERED_KEY,
                event
            );
            
            logger.info("📢 Published UserRegisteredEvent for user: {} ({})", user.getEmail(), user.getId());
        } catch (Exception e) {
            logger.error("❌ Failed to publish UserRegisteredEvent for user: {}", user.getEmail(), e);
        }
    }

    /**
     * Publishes UserUpdatedEvent when user profile is updated
     */
    public void publishUserUpdatedEvent(User user) {
        try {
            UserUpdatedEvent event = new UserUpdatedEvent(
                UUID.randomUUID().toString(),
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                LocalDateTime.now(),
                "profile"  // updatedFields
            );
            
            eventPublisher.publish(
                RabbitMQConstants.USER_EXCHANGE,
                RabbitMQConstants.USER_UPDATED_KEY,
                event
            );
            
            logger.info("📢 Published UserUpdatedEvent for user: {} ({})", user.getEmail(), user.getId());
        } catch (Exception e) {
            logger.error("❌ Failed to publish UserUpdatedEvent for user: {}", user.getEmail(), e);
        }
    }

    /**
     * Publishes RoleAssignedEvent when a role is assigned to user
     */
    public void publishRoleAssignedEvent(Long userId, String userEmail, String roleName) {
        try {
            RoleAssignedEvent event = new RoleAssignedEvent(
                UUID.randomUUID().toString(),
                userId.toString(),
                userEmail,
                roleName,
                "SYSTEM",  // assignedBy
                LocalDateTime.now()
            );
            
            eventPublisher.publish(
                RabbitMQConstants.USER_EXCHANGE,
                RabbitMQConstants.ROLE_ASSIGNED_KEY,
                event
            );
            
            logger.info("📢 Published RoleAssignedEvent for user: {} - Role: {}", userEmail, roleName);
        } catch (Exception e) {
            logger.error("❌ Failed to publish RoleAssignedEvent for user: {}", userEmail, e);
        }
    }
}
