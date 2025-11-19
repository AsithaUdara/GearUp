package com.gearup.shared.event.chatbot;

import com.gearup.shared.event.base.BaseChatbotEvent;

import java.time.LocalDateTime;

/**
 * Event published when a chat session is started
 */
public class ChatSessionStartedEvent extends BaseChatbotEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String customerName;
    private String customerEmail;
    private LocalDateTime startedAt;
    private String initialMessage;
    
    public ChatSessionStartedEvent() {
        super();
    }
    
    public ChatSessionStartedEvent(String eventId, String sessionId, String customerId,
                                  String customerName, String customerEmail,
                                  LocalDateTime startedAt, String initialMessage) {
        super(eventId, sessionId, customerId);
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.startedAt = startedAt;
        this.initialMessage = initialMessage;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public String getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }

    @Override
    public String toString() {
        return "ChatSessionStartedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", sessionId='" + getSessionId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", startedAt=" + startedAt +
                ", initialMessage='" + initialMessage + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
