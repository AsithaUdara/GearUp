package com.gearup.shared.event.tracking;

import com.gearup.shared.event.base.BaseTrackingEvent;

import java.time.LocalDateTime;

/**
 * Event published when a task is created
 */
public class TaskCreatedEvent extends BaseTrackingEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String serviceType;
    private String customer;
    private String vehicle;
    private Integer estimatedDuration;
    private LocalDateTime createdAt;
    private String notes;
    
    public TaskCreatedEvent() {
        super();
    }
    
    public TaskCreatedEvent(String eventId, String taskId, String assigneeId,
                           String serviceType, String customer, String vehicle,
                           Integer estimatedDuration, LocalDateTime createdAt, String notes) {
        super(eventId, taskId, assigneeId);
        this.serviceType = serviceType;
        this.customer = customer;
        this.vehicle = vehicle;
        this.estimatedDuration = estimatedDuration;
        this.createdAt = createdAt;
        this.notes = notes;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
