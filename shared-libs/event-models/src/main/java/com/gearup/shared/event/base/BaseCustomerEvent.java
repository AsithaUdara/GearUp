package com.gearup.shared.event.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all customer-related events
 */
public abstract class BaseCustomerEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private String customerId;
    private LocalDateTime timestamp;
    
    protected BaseCustomerEvent() {
    }
    
    protected BaseCustomerEvent(String eventId, String customerId, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.customerId = customerId;
        this.timestamp = timestamp;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
