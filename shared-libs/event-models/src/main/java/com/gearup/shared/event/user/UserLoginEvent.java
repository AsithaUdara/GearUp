package com.gearup.shared.event.user;

import com.gearup.shared.event.base.BaseUserEvent;

import java.time.LocalDateTime;

/**
 * Event published when a user logs in
 */
public class UserLoginEvent extends BaseUserEvent {
    
    private static final long serialVersionUID = 1L;
    
    private LocalDateTime loginAt;
    private String ipAddress;
    private String deviceInfo;
    private boolean successful;
    
    public UserLoginEvent() {
        super();
    }
    
    public UserLoginEvent(String eventId, String userId, String userEmail,
                         LocalDateTime loginAt, String ipAddress, String deviceInfo, boolean successful) {
        super(eventId, userId, userEmail);
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.successful = successful;
    }

    public LocalDateTime getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(LocalDateTime loginAt) {
        this.loginAt = loginAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
