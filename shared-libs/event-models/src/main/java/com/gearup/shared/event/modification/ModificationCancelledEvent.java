package com.gearup.shared.event.modification;

import com.gearup.shared.event.base.BaseModificationEvent;

import java.time.LocalDateTime;

/**
 * Event published when a modification request is cancelled
 */
public class ModificationCancelledEvent extends BaseModificationEvent {
    
    private static final long serialVersionUID = 1L;
    
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private String cancellationReason;
    private String modificationType;
    
    public ModificationCancelledEvent() {
        super();
    }
    
    public ModificationCancelledEvent(String eventId, Long modificationId, String customerId, String customerName,
                                     LocalDateTime cancelledAt, String cancelledBy, String cancellationReason, String modificationType) {
        super(eventId, modificationId, customerId, customerName);
        this.cancelledAt = cancelledAt;
        this.cancelledBy = cancelledBy;
        this.cancellationReason = cancellationReason;
        this.modificationType = modificationType;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getModificationType() {
        return modificationType;
    }

    public void setModificationType(String modificationType) {
        this.modificationType = modificationType;
    }
}
