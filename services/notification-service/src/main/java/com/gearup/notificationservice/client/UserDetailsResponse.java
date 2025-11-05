package com.gearup.notificationservice.client;

import java.io.Serializable;

/**
 * DTO for user details received from user-auth-service.
 */
public class UserDetailsResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String email;
    private String displayName;
    private String phoneNumber;
    private Boolean emailVerified;
    
    // Default constructor
    public UserDetailsResponse() {
    }
    
    // All-args constructor
    public UserDetailsResponse(String userId, String email, String displayName, String phoneNumber, Boolean emailVerified) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
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
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    @Override
    public String toString() {
        return "UserDetailsResponse{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", emailVerified=" + emailVerified +
                '}';
    }
}
