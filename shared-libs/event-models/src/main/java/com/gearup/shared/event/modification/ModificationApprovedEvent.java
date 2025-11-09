package com.gearup.shared.event.modification;

import com.gearup.shared.event.base.BaseModificationEvent;

import java.time.LocalDateTime;

/**
 * Event published when a modification request is approved
 */
public class ModificationApprovedEvent extends BaseModificationEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String modificationType;
    private String notes;
    
    public ModificationApprovedEvent() {
        super();
    }
    
    public ModificationApprovedEvent(String eventId, Long modificationId, String customerId, String customerName,
                                    String approvedBy, LocalDateTime approvedAt, String modificationType, String notes) {
        super(eventId, modificationId, customerId, customerName);
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.modificationType = modificationType;
        this.notes = notes;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getModificationType() {
        return modificationType;
    }

    public void setModificationType(String modificationType) {
        this.modificationType = modificationType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
