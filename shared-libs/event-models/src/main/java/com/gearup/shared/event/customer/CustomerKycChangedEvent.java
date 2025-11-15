package com.gearup.shared.event.customer;

import com.gearup.shared.event.base.BaseCustomerEvent;

import java.time.LocalDateTime;

/**
 * Event published when customer KYC status changes
 */
public class CustomerKycChangedEvent extends BaseCustomerEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String oldStatus;
    private String newStatus;
    private String reason;

    public CustomerKycChangedEvent() {
        super();
    }

    public CustomerKycChangedEvent(String eventId, String customerId, LocalDateTime timestamp,
                                  String oldStatus, String newStatus, String reason) {
        super(eventId, customerId, timestamp);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "CustomerKycChangedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", reason='" + reason + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
