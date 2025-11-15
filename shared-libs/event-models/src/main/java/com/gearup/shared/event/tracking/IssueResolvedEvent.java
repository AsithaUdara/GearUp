package com.gearup.shared.event.tracking;

import com.gearup.shared.event.base.BaseTrackingEvent;

import java.time.LocalDateTime;

/**
 * Event published when a reported issue is resolved
 */
public class IssueResolvedEvent extends BaseTrackingEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String issueType;
    private String resolution;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String notes;
    
    public IssueResolvedEvent() {
        super();
    }
    
    public IssueResolvedEvent(String eventId, String taskId, String assigneeId,
                             String issueType, String resolution, LocalDateTime resolvedAt,
                             String resolvedBy, String notes) {
        super(eventId, taskId, assigneeId);
        this.issueType = issueType;
        this.resolution = resolution;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.notes = notes;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
