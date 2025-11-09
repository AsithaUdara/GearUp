package com.gearup.shared.event.user;

import com.gearup.shared.event.base.BaseUserEvent;

import java.time.LocalDateTime;

/**
 * Event published when a new user registers
 */
public class UserRegisteredEvent extends BaseUserEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String userName;
    private String userRole;
    private LocalDateTime registeredAt;
    private String registrationMethod;
    
    public UserRegisteredEvent() {
        super();
    }
    
    public UserRegisteredEvent(String eventId, String userId, String userEmail,
                              String userName, String userRole, LocalDateTime registeredAt, String registrationMethod) {
        super(eventId, userId, userEmail);
        this.userName = userName;
        this.userRole = userRole;
        this.registeredAt = registeredAt;
        this.registrationMethod = registrationMethod;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getRegistrationMethod() {
        return registrationMethod;
    }

    public void setRegistrationMethod(String registrationMethod) {
        this.registrationMethod = registrationMethod;
    }
}
