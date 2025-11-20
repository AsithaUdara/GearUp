package com.gearup.shared.event.parts;

import com.gearup.shared.event.base.BasePartsEvent;

import java.time.LocalDateTime;

/**
 * Event published when parts status changes
 */
public class PartsStatusChangedEvent extends BasePartsEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String partName;
    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
    
    public PartsStatusChangedEvent() {
        super();
    }
    
    public PartsStatusChangedEvent(String eventId, Long partId, String partName,
                                  String oldStatus, String newStatus,
                                  String changedBy, LocalDateTime changedAt) {
        super(eventId, partId);
        this.partName = partName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
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

    @Override
    public String toString() {
        return "PartsStatusChangedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", partId='" + getPartId() + '\'' +
                ", partName='" + getPartName() + '\'' +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
