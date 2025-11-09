package com.gearup.shared.event.modification;

import com.gearup.shared.event.base.BaseModificationEvent;

import java.time.LocalDateTime;

/**
 * Event published when a modification request is rejected
 */
public class ModificationRejectedEvent extends BaseModificationEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private String modificationType;
    
    public ModificationRejectedEvent() {
        super();
    }
    
    public ModificationRejectedEvent(String eventId, Long modificationId, String customerId, String customerName,
                                    String rejectedBy, LocalDateTime rejectedAt, String rejectionReason, String modificationType) {
        super(eventId, modificationId, customerId, customerName);
        this.rejectedBy = rejectedBy;
        this.rejectedAt = rejectedAt;
        this.rejectionReason = rejectionReason;
        this.modificationType = modificationType;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getModificationType() {
        return modificationType;
    }

    public void setModificationType(String modificationType) {
        this.modificationType = modificationType;
    }
}
