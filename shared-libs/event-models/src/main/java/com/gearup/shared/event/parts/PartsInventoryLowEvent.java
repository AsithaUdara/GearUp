package com.gearup.shared.event.parts;

import com.gearup.shared.event.base.BasePartsEvent;

import java.time.LocalDateTime;

/**
 * Event published when parts inventory falls below minimum threshold
 */
public class PartsInventoryLowEvent extends BasePartsEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String partName;
    private int currentQuantity;
    private int minimumQuantity;
    private String supplier;
    private LocalDateTime detectedAt;
    
    public PartsInventoryLowEvent() {
        super();
    }
    
    public PartsInventoryLowEvent(String eventId, Long partId, String partName, 
                                 int currentQuantity, int minimumQuantity, 
                                 String supplier, LocalDateTime detectedAt) {
        super(eventId, partId);
        this.partName = partName;
        this.currentQuantity = currentQuantity;
        this.minimumQuantity = minimumQuantity;
        this.supplier = supplier;
        this.detectedAt = detectedAt;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    // Getters and Setters
    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    @Override
    public String toString() {
        return "PartsInventoryLowEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", partId='" + getPartId() + '\'' +
                ", partName='" + getPartName() + '\'' +
                ", currentQuantity=" + currentQuantity +
                ", minimumQuantity=" + minimumQuantity +
                ", supplier='" + supplier + '\'' +
                ", detectedAt=" + detectedAt +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
