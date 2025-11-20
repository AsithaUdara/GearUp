package com.gearup.shared.event.modification;

import com.gearup.shared.event.base.BaseModificationEvent;

import java.time.LocalDateTime;

/**
 * Event published when a new modification request is created
 */
public class ModificationRequestCreatedEvent extends BaseModificationEvent {
    
    private String customerEmail;
    private LocalDateTime requestDate;
    private String serviceName;
    private Double estimatedCost;
    private String preferredDate;
    
    public ModificationRequestCreatedEvent() {
        super();
    }
    
    public ModificationRequestCreatedEvent(
            String eventId,
            Long requestId,
            String customerId,
            String customerName,
            String customerEmail,
            LocalDateTime requestDate,
            String serviceName,
            Double estimatedCost,
            String preferredDate) {
        super(eventId, requestId, customerId, customerName);
        this.customerEmail = customerEmail;
        this.requestDate = requestDate;
        this.serviceName = serviceName;
        this.estimatedCost = estimatedCost;
        this.preferredDate = preferredDate;
    }

    // Getters and Setters
    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    @Override
    public String toString() {
        return "ModificationRequestCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", requestId=" + getModificationId() +
                ", customerId='" + getCustomerId() + '\'' +
                ", customerName='" + getCustomerName() + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", estimatedCost=" + estimatedCost +
                ", preferredDate='" + preferredDate + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
