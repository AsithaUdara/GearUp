package com.gearup.shared.event.user;

import com.gearup.shared.event.base.BaseUserEvent;

import java.time.LocalDateTime;

/**
 * Event published when a user profile is updated
 */
public class UserUpdatedEvent extends BaseUserEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String userName;
    private String phoneNumber;
    private LocalDateTime updatedAt;
    private String updatedFields;
    
    public UserUpdatedEvent() {
        super();
    }
    
    public UserUpdatedEvent(String eventId, String userId, String userEmail,
                           String userName, String phoneNumber, LocalDateTime updatedAt, String updatedFields) {
        super(eventId, userId, userEmail);
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.updatedAt = updatedAt;
        this.updatedFields = updatedFields;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedFields() {
        return updatedFields;
    }

    public void setUpdatedFields(String updatedFields) {
        this.updatedFields = updatedFields;
    }
}
