package com.gearup.shared.event.chatbot;

import com.gearup.shared.event.base.BaseChatbotEvent;

import java.time.LocalDateTime;

/**
 * Event published when customer requests escalation to human support
 */
public class CustomerEscalationRequestedEvent extends BaseChatbotEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String customerName;
    private String customerEmail;
    private LocalDateTime requestedAt;
    private String escalationReason;
    private String lastBotResponse;
    private String urgencyLevel;
    
    public CustomerEscalationRequestedEvent() {
        super();
    }
    
    public CustomerEscalationRequestedEvent(String eventId, String sessionId, String customerId,
                                           String customerName, String customerEmail,
                                           LocalDateTime requestedAt, String escalationReason,
                                           String lastBotResponse, String urgencyLevel) {
        super(eventId, sessionId, customerId);
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.requestedAt = requestedAt;
        this.escalationReason = escalationReason;
        this.lastBotResponse = lastBotResponse;
        this.urgencyLevel = urgencyLevel;
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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public String getLastBotResponse() {
        return lastBotResponse;
    }

    public void setLastBotResponse(String lastBotResponse) {
        this.lastBotResponse = lastBotResponse;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    @Override
    public String toString() {
        return "CustomerEscalationRequestedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", sessionId='" + getSessionId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", requestedAt=" + requestedAt +
                ", escalationReason='" + escalationReason + '\'' +
                ", lastBotResponse='" + lastBotResponse + '\'' +
                ", urgencyLevel='" + urgencyLevel + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
