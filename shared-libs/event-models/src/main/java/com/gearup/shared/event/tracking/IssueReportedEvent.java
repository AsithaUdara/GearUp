package com.gearup.shared.event.tracking;

import com.gearup.shared.event.base.BaseTrackingEvent;

import java.time.LocalDateTime;

/**
 * Event published when an issue is reported during task execution
 */
public class IssueReportedEvent extends BaseTrackingEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String issueType;
    private String issueDescription;
    private String severity;
    private LocalDateTime reportedAt;
    private String reportedBy;
    private boolean requiresApproval;
    
    public IssueReportedEvent() {
        super();
    }
    
    public IssueReportedEvent(String eventId, String taskId, String assigneeId,
                             String issueType, String issueDescription, String severity,
                             LocalDateTime reportedAt, String reportedBy, boolean requiresApproval) {
        super(eventId, taskId, assigneeId);
        this.issueType = issueType;
        this.issueDescription = issueDescription;
        this.severity = severity;
        this.reportedAt = reportedAt;
        this.reportedBy = reportedBy;
        this.requiresApproval = requiresApproval;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }
}
