package com.gearup.shared.event.tracking;

import com.gearup.shared.event.base.BaseTrackingEvent;

/**
 * Event published when a task is completed.
 */
public class TaskCompletedEvent extends BaseTrackingEvent {
    private String taskTitle;
    private String completedBy;

    public TaskCompletedEvent() {
    }

    public TaskCompletedEvent(String eventId, String taskId, String assigneeId,
                              String taskTitle, String completedBy) {
        super(eventId, taskId, assigneeId);
        this.taskTitle = taskTitle;
        this.completedBy = completedBy;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(String completedBy) {
        this.completedBy = completedBy;
    }
}
