package com.gearup.shared.event;

/**
 * Event published when a task is completed.
 */
public class TaskCompletedEvent extends BaseNotificationEvent {
    private String taskId;
    private String taskTitle;
    private String completedBy;

    public TaskCompletedEvent() {
    }

    public TaskCompletedEvent(String eventId, String userId, java.time.LocalDateTime timestamp,
                              String taskId, String taskTitle, String completedBy) {
        super(eventId, userId, timestamp);
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.completedBy = completedBy;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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
