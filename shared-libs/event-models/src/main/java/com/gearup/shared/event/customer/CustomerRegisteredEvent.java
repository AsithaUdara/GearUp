package com.gearup.shared.event.customer;

import com.gearup.shared.event.base.BaseCustomerEvent;

import java.time.LocalDateTime;

/**
 * Event published when a new customer registers
 */
public class CustomerRegisteredEvent extends BaseCustomerEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String email;
    private String displayName;
    private String phone;

    public CustomerRegisteredEvent() {
        super();
    }

    public CustomerRegisteredEvent(String eventId, String customerId, LocalDateTime timestamp,
                                   String email, String displayName, String phone) {
        super(eventId, customerId, timestamp);
        this.email = email;
        this.displayName = displayName;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "CustomerRegisteredEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", phone='" + phone + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
