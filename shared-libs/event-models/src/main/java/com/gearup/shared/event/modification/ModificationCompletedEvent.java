package com.gearup.shared.event.modification;

import com.gearup.shared.event.base.BaseModificationEvent;

import java.time.LocalDateTime;

/**
 * Event published when a modification request is completed
 */
public class ModificationCompletedEvent extends BaseModificationEvent {
    
    private static final long serialVersionUID = 1L;
    
    private LocalDateTime completedAt;
    private String completedBy;
    private String modificationType;
    private Double finalCost;
    private String notes;
    
    public ModificationCompletedEvent() {
        super();
    }
    
    public ModificationCompletedEvent(String eventId, Long modificationId, String customerId, String customerName,
                                     LocalDateTime completedAt, String completedBy, String modificationType, 
                                     Double finalCost, String notes) {
        super(eventId, modificationId, customerId, customerName);
        this.completedAt = completedAt;
        this.completedBy = completedBy;
        this.modificationType = modificationType;
        this.finalCost = finalCost;
        this.notes = notes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }

    public String getModificationType() {
        return modificationType;
    }

    public void setModificationType(String modificationType) {
        this.modificationType = modificationType;
    }

    public Double getFinalCost() {
        return finalCost;
    }

    public void setFinalCost(Double finalCost) {
        this.finalCost = finalCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
