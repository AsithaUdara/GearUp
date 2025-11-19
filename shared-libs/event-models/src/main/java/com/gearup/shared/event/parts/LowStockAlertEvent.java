package com.gearup.shared.event.parts;

import com.gearup.shared.event.base.BasePartsEvent;

import java.time.LocalDateTime;

/**
 * Event published when part stock falls below threshold
 */
public class LowStockAlertEvent extends BasePartsEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String partName;
    private Integer currentStock;
    private Integer threshold;
    private LocalDateTime alertedAt;
    private String severity;
    
    public LowStockAlertEvent() {
        super();
    }
    
    public LowStockAlertEvent(String eventId, Long partId, String partName,
                             Integer currentStock, Integer threshold,
                             LocalDateTime alertedAt, String severity) {
        super(eventId, partId);
        this.partName = partName;
        this.currentStock = currentStock;
        this.threshold = threshold;
        this.alertedAt = alertedAt;
        this.severity = severity;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public LocalDateTime getAlertedAt() {
        return alertedAt;
    }

    public void setAlertedAt(LocalDateTime alertedAt) {
        this.alertedAt = alertedAt;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
