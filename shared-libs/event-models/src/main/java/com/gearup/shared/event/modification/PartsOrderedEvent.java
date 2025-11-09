package com.gearup.shared.event.modification;

import com.gearup.shared.event.base.BaseModificationEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when parts are ordered for a modification
 */
public class PartsOrderedEvent extends BaseModificationEvent {
    
    private static final long serialVersionUID = 1L;
    
    private List<String> partIds;
    private List<String> partNames;
    private Double totalCost;
    private LocalDateTime orderedAt;
    private String orderedBy;
    private String supplierId;
    private String estimatedDelivery;
    
    public PartsOrderedEvent() {
        super();
    }
    
    public PartsOrderedEvent(String eventId, Long modificationId, String customerId, String customerName,
                            List<String> partIds, List<String> partNames, Double totalCost, 
                            LocalDateTime orderedAt, String orderedBy, String supplierId, String estimatedDelivery) {
        super(eventId, modificationId, customerId, customerName);
        this.partIds = partIds;
        this.partNames = partNames;
        this.totalCost = totalCost;
        this.orderedAt = orderedAt;
        this.orderedBy = orderedBy;
        this.supplierId = supplierId;
        this.estimatedDelivery = estimatedDelivery;
    }

    public List<String> getPartIds() {
        return partIds;
    }

    public void setPartIds(List<String> partIds) {
        this.partIds = partIds;
    }

    public List<String> getPartNames() {
        return partNames;
    }

    public void setPartNames(List<String> partNames) {
        this.partNames = partNames;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }
}
