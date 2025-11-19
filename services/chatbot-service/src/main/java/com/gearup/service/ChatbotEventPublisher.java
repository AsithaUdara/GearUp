package com.gearup.service;

import com.gearup.shared.event.chatbot.ChatSessionClosedEvent;
import com.gearup.shared.event.chatbot.ChatSessionStartedEvent;
import com.gearup.shared.event.chatbot.CustomerEscalationRequestedEvent;
import com.gearup.shared.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event Publisher Service for Chatbot Service
 * Publishes chatbot-related events to RabbitMQ for inter-service communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotEventPublisher {

    private final com.gearup.shared.messaging.EventPublisher eventPublisher;

    /**
     * Publish event when a chat session is started
     */
    public void publishChatSessionStartedEvent(String sessionId, String customerId, String customerName,
                                               String customerEmail, String initialMessage) {
        try {
            String eventId = UUID.randomUUID().toString();
            ChatSessionStartedEvent event = new ChatSessionStartedEvent(
                eventId,
                sessionId,
                customerId,
                customerName,
                customerEmail,
                LocalDateTime.now(),
                initialMessage
            );
            
            eventPublisher.publish(
                RabbitMQConstants.CHATBOT_EXCHANGE,
                RabbitMQConstants.CHAT_SESSION_STARTED_KEY,
                event,
                eventId
            );
            
            log.info("📤 Published ChatSessionStartedEvent for customer: {} (session: {})", 
                customerName, sessionId);
        } catch (Exception e) {
            log.error("❌ Failed to publish ChatSessionStartedEvent for customer: {}", customerName, e);
        }
    }

    /**
     * Publish event when a chat session is closed
     */
    public void publishChatSessionClosedEvent(String sessionId, String customerId, String customerName,
                                             String closureReason, int messageCount, boolean wasResolved,
                                             int durationMinutes) {
        try {
            String eventId = UUID.randomUUID().toString();
            ChatSessionClosedEvent event = new ChatSessionClosedEvent(
                eventId,
                sessionId,
                customerId,
                customerName,
                LocalDateTime.now(),
                closureReason,
                messageCount,
                wasResolved,
                durationMinutes
            );
            
            eventPublisher.publish(
                RabbitMQConstants.CHATBOT_EXCHANGE,
                RabbitMQConstants.CHAT_SESSION_CLOSED_KEY,
                event,
                eventId
            );
            
            log.info("📤 Published ChatSessionClosedEvent for customer: {} (session: {}, resolved: {})", 
                customerName, sessionId, wasResolved);
        } catch (Exception e) {
            log.error("❌ Failed to publish ChatSessionClosedEvent for customer: {}", customerName, e);
        }
    }

    /**
     * Publish event when customer requests escalation to human support
     */
    public void publishCustomerEscalationRequestedEvent(String sessionId, String customerId, String customerName,
                                                        String customerEmail, String escalationReason,
                                                        String lastBotResponse, String urgencyLevel) {
        try {
            String eventId = UUID.randomUUID().toString();
            CustomerEscalationRequestedEvent event = new CustomerEscalationRequestedEvent(
                eventId,
                sessionId,
                customerId,
                customerName,
                customerEmail,
                LocalDateTime.now(),
                escalationReason,
                lastBotResponse,
                urgencyLevel
            );
            
            eventPublisher.publish(
                RabbitMQConstants.CHATBOT_EXCHANGE,
                RabbitMQConstants.CUSTOMER_ESCALATION_REQUESTED_KEY,
                event,
                eventId
            );
            
            log.info("📤 Published CustomerEscalationRequestedEvent for customer: {} (session: {}, urgency: {})", 
                customerName, sessionId, urgencyLevel);
        } catch (Exception e) {
            log.error("❌ Failed to publish CustomerEscalationRequestedEvent for customer: {}", customerName, e);
        }
    }
}
