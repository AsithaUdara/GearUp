package com.gearup.notificationservice.listener;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.chatbot.ChatSessionClosedEvent;
import com.gearup.shared.event.chatbot.ChatSessionStartedEvent;
import com.gearup.shared.event.chatbot.CustomerEscalationRequestedEvent;
import com.gearup.shared.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for chatbot-related events and sends notifications
 */
//@Component  // Temporarily disabled - events handled by consolidated dispatcher
@RequiredArgsConstructor
@Slf4j
public class ChatbotEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleMessage(org.springframework.messaging.Message<?> message) {
        Object payload = message.getPayload();

        try {
            if (payload instanceof ChatSessionStartedEvent) {
                handleChatSessionStarted((ChatSessionStartedEvent) payload);
            } else if (payload instanceof ChatSessionClosedEvent) {
                handleChatSessionClosed((ChatSessionClosedEvent) payload);
            } else if (payload instanceof CustomerEscalationRequestedEvent) {
                handleCustomerEscalationRequested((CustomerEscalationRequestedEvent) payload);
            }
            // Silently ignore other event types
        } catch (Exception e) {
            log.error("Error processing chatbot event: {}", payload.getClass().getName(), e);
            throw e;
        }
    }

    private void handleChatSessionStarted(ChatSessionStartedEvent event) {
        try {
            log.info("🔔 Received ChatSessionStartedEvent: sessionId={}, customer={}",
                    event.getSessionId(), event.getCustomerName());

            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Chat Session Started");
            notification.setMessage(String.format("Welcome %s! Your chat session has started. How can we help you today?",
                    event.getCustomerName()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.LOW);
            notification.setRelatedEntityId(event.getSessionId());
            notification.setRelatedEntityType("CHAT_SESSION");
            notification.setDeliveryChannels("[\"WEB\"]");

            notificationService.createNotification(notification);
            log.info("✅ Sent chat session started notification to customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("❌ Failed to send chat session started notification for session: {}", event.getSessionId(), e);
        }
    }

    private void handleChatSessionClosed(ChatSessionClosedEvent event) {
        try {
            log.info("🔔 Received ChatSessionClosedEvent: sessionId={}, customer={}, resolved={}",
                    event.getSessionId(), event.getCustomerName(), event.isWasResolved());

            String message;
            if (event.isWasResolved()) {
                message = String.format("Thank you %s! Your chat session has ended. We're glad we could help you today. Session duration: %d minutes.",
                        event.getCustomerName(), event.getDurationMinutes());
            } else {
                message = String.format("Thank you %s! Your chat session has ended. If you need further assistance, please don't hesitate to reach out again.",
                        event.getCustomerName());
            }

            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Chat Session Closed");
            notification.setMessage(message);
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.LOW);
            notification.setRelatedEntityId(event.getSessionId());
            notification.setRelatedEntityType("CHAT_SESSION");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");

            notificationService.createNotification(notification);
            log.info("✅ Sent chat session closed notification to customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("❌ Failed to send chat session closed notification for session: {}", event.getSessionId(), e);
        }
    }

    private void handleCustomerEscalationRequested(CustomerEscalationRequestedEvent event) {
        try {
            log.info("🔔 Received CustomerEscalationRequestedEvent: sessionId={}, customer={}, urgency={}",
                    event.getSessionId(), event.getCustomerName(), event.getUrgencyLevel());

            // Send notification to customer
            NotificationRequest customerNotification = new NotificationRequest();
            customerNotification.setUserId(event.getCustomerId());
            customerNotification.setTitle("Escalation Request Received");
            customerNotification.setMessage(String.format("Hi %s, we've received your request for human assistance. A support agent will be with you shortly.",
                    event.getCustomerName()));
            customerNotification.setType(NotificationType.GENERAL);
            customerNotification.setPriority(NotificationPriority.MEDIUM);
            customerNotification.setRelatedEntityId(event.getSessionId());
            customerNotification.setRelatedEntityType("CHAT_SESSION");
            customerNotification.setDeliveryChannels("[\"WEB\"]");

            notificationService.createNotification(customerNotification);

            // Send alert to support team
            NotificationRequest supportAlert = new NotificationRequest();
            supportAlert.setUserId("SUPPORT_TEAM"); // Send to support team
            supportAlert.setTitle("⚠️ Customer Escalation Request");
            supportAlert.setMessage(String.format("Customer %s (ID: %s) has requested escalation. Reason: %s. Urgency: %s. Session ID: %s",
                    event.getCustomerName(), event.getCustomerId(), event.getEscalationReason(), event.getUrgencyLevel(), event.getSessionId()));
            supportAlert.setType(NotificationType.TASK_ASSIGNED);
            supportAlert.setPriority(determineEscalationPriority(event.getUrgencyLevel()));
            supportAlert.setRelatedEntityId(event.getSessionId());
            supportAlert.setRelatedEntityType("CHAT_SESSION");
            supportAlert.setDeliveryChannels(determineEscalationChannels(event.getUrgencyLevel()));

            notificationService.createNotification(supportAlert);
            log.info("✅ Sent escalation notifications for customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("❌ Failed to send escalation notifications for session: {}", event.getSessionId(), e);
        }
    }

    /**
     * Determine notification priority based on escalation urgency
     */
    private NotificationPriority determineEscalationPriority(String urgencyLevel) {
        return switch (urgencyLevel.toUpperCase()) {
            case "URGENT", "CRITICAL" -> NotificationPriority.HIGH;
            case "NORMAL" -> NotificationPriority.MEDIUM;
            default -> NotificationPriority.LOW;
        };
    }

    /**
     * Determine notification channels based on escalation urgency
     */
    private String determineEscalationChannels(String urgencyLevel) {
        return switch (urgencyLevel.toUpperCase()) {
            case "URGENT", "CRITICAL" -> "[\"WEB\", \"EMAIL\", \"SMS\"]";
            case "NORMAL" -> "[\"WEB\", \"EMAIL\"]";
            default -> "[\"WEB\"]";
        };
    }
}
