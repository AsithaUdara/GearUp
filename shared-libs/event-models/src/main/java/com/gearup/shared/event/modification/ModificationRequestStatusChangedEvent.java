package com.gearup.shared.event.modification;

import com.gearup.shared.event.base.BaseModificationEvent;

import java.time.LocalDateTime;

/**
 * Event published when a modification request status changes
 */
public class ModificationRequestStatusChangedEvent extends BaseModificationEvent {
    
    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
    private String serviceName;
    private String notes;
    
    public ModificationRequestStatusChangedEvent() {
        super();
    }
    
    public ModificationRequestStatusChangedEvent(
            String eventId,
            Long requestId,
            String customerId,
            String customerName,
            String oldStatus,
            String newStatus,
            String changedBy,
            LocalDateTime changedAt,
            String serviceName,
            String notes) {
        super(eventId, requestId, customerId, customerName);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.serviceName = serviceName;
        this.notes = notes;
    }

    // Getters and Setters
    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "ModificationRequestStatusChangedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", requestId=" + getModificationId() +
                ", customerId='" + getCustomerId() + '\'' +
                ", customerName='" + getCustomerName() + '\'' +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
