package com.gearup.shared.event.user;

import com.gearup.shared.event.base.BaseUserEvent;

import java.time.LocalDateTime;

/**
 * Event published when a role is assigned to a user
 */
public class RoleAssignedEvent extends BaseUserEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String roleName;
    private String assignedBy;
    private LocalDateTime assignedAt;
    
    public RoleAssignedEvent() {
        super();
    }
    
    public RoleAssignedEvent(String eventId, String userId, String userEmail,
                            String roleName, String assignedBy, LocalDateTime assignedAt) {
        super(eventId, userId, userEmail);
        this.roleName = roleName;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
