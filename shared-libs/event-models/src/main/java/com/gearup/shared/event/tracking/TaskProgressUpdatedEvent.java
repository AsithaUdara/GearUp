package com.gearup.shared.event.tracking;

import com.gearup.shared.event.base.BaseTrackingEvent;

import java.time.LocalDateTime;

/**
 * Event published when task progress is updated
 */
public class TaskProgressUpdatedEvent extends BaseTrackingEvent {
    
    private static final long serialVersionUID = 1L;
    
    private Integer progressStep;
    private String status;
    private LocalDateTime updatedAt;
    private String notes;
    private Integer percentComplete;
    
    public TaskProgressUpdatedEvent() {
        super();
    }
    
    public TaskProgressUpdatedEvent(String eventId, String taskId, String assigneeId,
                                   Integer progressStep, String status, LocalDateTime updatedAt,
                                   String notes, Integer percentComplete) {
        super(eventId, taskId, assigneeId);
        this.progressStep = progressStep;
        this.status = status;
        this.updatedAt = updatedAt;
        this.notes = notes;
        this.percentComplete = percentComplete;
    }

    public Integer getProgressStep() {
        return progressStep;
    }

    public void setProgressStep(Integer progressStep) {
        this.progressStep = progressStep;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
    }
}
