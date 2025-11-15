package com.gearup.shared.event.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all payment-related events
 */
public abstract class BasePaymentEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private LocalDateTime timestamp;
    private String source;
    private String userId;
    
    protected BasePaymentEvent() {
        this.timestamp = LocalDateTime.now();
        this.source = "payment-service";
    }
    
    protected BasePaymentEvent(String eventId, String userId, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.source = "payment-service";
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
