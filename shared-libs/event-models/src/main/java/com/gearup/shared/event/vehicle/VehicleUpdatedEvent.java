package com.gearup.shared.event.vehicle;

import com.gearup.shared.event.base.BaseVehicleEvent;

import java.time.LocalDateTime;

/**
 * Event published when vehicle information is updated
 */
public class VehicleUpdatedEvent extends BaseVehicleEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String updatedField;
    private String oldValue;
    private String newValue;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    public VehicleUpdatedEvent() {
        super();
    }
    
    public VehicleUpdatedEvent(String eventId, Long vehicleId, String customerId,
                              String updatedField, String oldValue, String newValue,
                              LocalDateTime updatedAt, String updatedBy) {
        super(eventId, vehicleId, customerId);
        this.updatedField = updatedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getUpdatedField() {
        return updatedField;
    }

    public void setUpdatedField(String updatedField) {
        this.updatedField = updatedField;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
