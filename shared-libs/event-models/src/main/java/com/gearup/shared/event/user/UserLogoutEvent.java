package com.gearup.shared.event.user;

import com.gearup.shared.event.base.BaseUserEvent;

import java.time.LocalDateTime;

/**
 * Event published when a user logs out
 */
public class UserLogoutEvent extends BaseUserEvent {
    
    private static final long serialVersionUID = 1L;
    
    private LocalDateTime logoutAt;
    private Integer sessionDuration;
    
    public UserLogoutEvent() {
        super();
    }
    
    public UserLogoutEvent(String eventId, String userId, String userEmail,
                          LocalDateTime logoutAt, Integer sessionDuration) {
        super(eventId, userId, userEmail);
        this.logoutAt = logoutAt;
        this.sessionDuration = sessionDuration;
    }

    public LocalDateTime getLogoutAt() {
        return logoutAt;
    }

    public void setLogoutAt(LocalDateTime logoutAt) {
        this.logoutAt = logoutAt;
    }

    public Integer getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(Integer sessionDuration) {
        this.sessionDuration = sessionDuration;
    }
}
