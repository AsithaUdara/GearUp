package com.gearup.shared.event.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all vehicle-related events
 */
public abstract class BaseVehicleEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private LocalDateTime timestamp;
    private String source;
    private Long vehicleId;
    private String customerId;
    
    public BaseVehicleEvent() {
        this.timestamp = LocalDateTime.now();
        this.source = "vehicle-service";
    }
    
    public BaseVehicleEvent(String eventId, Long vehicleId, String customerId) {
        this();
        this.eventId = eventId;
        this.vehicleId = vehicleId;
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

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
