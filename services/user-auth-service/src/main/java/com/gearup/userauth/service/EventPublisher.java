package com.gearup.userauth.service;

import com.gearup.userauth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for publishing user-related events to message broker (RabbitMQ).
 * These events can be consumed by other microservices.
 */
@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    // TODO: Inject RabbitTemplate or Spring Cloud Stream when message broker is configured
    // private final RabbitTemplate rabbitTemplate;

    public void publishUserRegisteredEvent(User user) {
        // TODO: Publish to message broker
        // Example:
        // UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), user.getEmail(), user.getFirebaseUid());
        // rabbitTemplate.convertAndSend("user.exchange", "user.registered", event);
        
        logger.info("User registered event published for user: {}", user.getEmail());
    }

    public void publishUserUpdatedEvent(User user) {
        // TODO: Publish to message broker
        // Example:
        // UserUpdatedEvent event = new UserUpdatedEvent(user.getId(), user.getEmail());
        // rabbitTemplate.convertAndSend("user.exchange", "user.updated", event);
        
        logger.info("User updated event published for user: {}", user.getEmail());
    }

    public void publishRoleAssignedEvent(Long userId, String roleName) {
        // TODO: Publish to message broker
        // Example:
        // RoleAssignedEvent event = new RoleAssignedEvent(userId, roleName);
        // rabbitTemplate.convertAndSend("user.exchange", "user.role.assigned", event);
        
        logger.info("Role assigned event published for user {}: {}", userId, roleName);
    }
}
