package com.gearup.shared.event.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all chatbot-related events
 */
public abstract class BaseChatbotEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private LocalDateTime timestamp;
    private String source;
    private String sessionId;
    private String customerId;
    
    public BaseChatbotEvent() {
        this.timestamp = LocalDateTime.now();
        this.source = "chatbot-service";
    }
    
    public BaseChatbotEvent(String eventId, String sessionId, String customerId) {
        this();
        this.eventId = eventId;
        this.sessionId = sessionId;
        this.customerId = customerId;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
