package com.gearup.shared.event.parts;

import com.gearup.shared.event.base.BasePartsEvent;

import java.time.LocalDateTime;

/**
 * Event published when parts stock level changes
 */
public class PartStockUpdatedEvent extends BasePartsEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String partName;
    private Integer previousStock;
    private Integer currentStock;
    private String changeReason;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    public PartStockUpdatedEvent() {
        super();
    }
    
    public PartStockUpdatedEvent(String eventId, Long partId, String partName,
                                Integer previousStock, Integer currentStock,
                                String changeReason, LocalDateTime updatedAt, String updatedBy) {
        super(eventId, partId);
        this.partName = partName;
        this.previousStock = previousStock;
        this.currentStock = currentStock;
        this.changeReason = changeReason;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
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
