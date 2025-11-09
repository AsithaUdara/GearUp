package com.gearup.shared.event.customer;

import com.gearup.shared.event.base.BaseCustomerEvent;

import java.time.LocalDateTime;

/**
 * Event published when customer information is updated
 */
public class CustomerUpdatedEvent extends BaseCustomerEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String email;
    private String displayName;
    private String phone;
    private String address;

    public CustomerUpdatedEvent() {
        super();
    }

    public CustomerUpdatedEvent(String eventId, String customerId, LocalDateTime timestamp,
                               String email, String displayName, String phone, String address) {
        super(eventId, customerId, timestamp);
        this.email = email;
        this.displayName = displayName;
        this.phone = phone;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "CustomerUpdatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", customerId='" + getCustomerId() + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
