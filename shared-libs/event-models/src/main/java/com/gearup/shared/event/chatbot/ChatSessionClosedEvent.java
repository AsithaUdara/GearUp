package com.gearup.shared.event.chatbot;

import com.gearup.shared.event.base.BaseChatbotEvent;

import java.time.LocalDateTime;

/**
 * Event published when a chat session is closed
 */
public class ChatSessionClosedEvent extends BaseChatbotEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String customerName;
    private LocalDateTime closedAt;
    private String closureReason;
    private int messageCount;
    private boolean wasResolved;
    private int durationMinutes;
    
    public ChatSessionClosedEvent() {
        super();
    }
    
    public ChatSessionClosedEvent(String eventId, String sessionId, String customerId,
                                 String customerName, LocalDateTime closedAt,
                                 String closureReason, int messageCount,
                                 boolean wasResolved, int durationMinutes) {
        super(eventId, sessionId, customerId);
        this.customerName = customerName;
        this.closedAt = closedAt;
        this.closureReason = closureReason;
        this.messageCount = messageCount;
        this.wasResolved = wasResolved;
        this.durationMinutes = durationMinutes;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getClosureReason() {
        return closureReason;
    }

    public void setClosureReason(String closureReason) {
        this.closureReason = closureReason;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public boolean isWasResolved() {
        return wasResolved;
    }

    public void setWasResolved(boolean wasResolved) {
        this.wasResolved = wasResolved;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    @Override
    public String toString() {
        return "ChatSessionClosedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", sessionId='" + getSessionId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", customerName='" + customerName + '\'' +
                ", closedAt=" + closedAt +
                ", closureReason='" + closureReason + '\'' +
                ", messageCount=" + messageCount +
                ", wasResolved=" + wasResolved +
                ", durationMinutes=" + durationMinutes +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
