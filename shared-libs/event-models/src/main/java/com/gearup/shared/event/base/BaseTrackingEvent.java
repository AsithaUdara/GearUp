package com.gearup.shared.event.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all tracking/task-related events
 */
public abstract class BaseTrackingEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private LocalDateTime timestamp;
    private String source;
    private String taskId;
    private String assigneeId;
    
    public BaseTrackingEvent() {
        this.timestamp = LocalDateTime.now();
        this.source = "tracking-service";
    }
    
    public BaseTrackingEvent(String eventId, String taskId, String assigneeId) {
        this();
        this.eventId = eventId;
        this.taskId = taskId;
        this.assigneeId = assigneeId;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }
}
