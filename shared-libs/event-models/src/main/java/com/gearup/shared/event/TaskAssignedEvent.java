package com.gearup.shared.event;

/**
 * Event published when a task is assigned.
 */
public class TaskAssignedEvent extends BaseNotificationEvent {
    private String taskId;
    private String taskTitle;
    private String assignedBy;
    private String dueDate;
    private String priority;

    public TaskAssignedEvent() {
    }

    public TaskAssignedEvent(String eventId, String userId, java.time.LocalDateTime timestamp,
                             String taskId, String taskTitle, String assignedBy, String dueDate, String priority) {
        super(eventId, userId, timestamp);
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.assignedBy = assignedBy;
        this.dueDate = dueDate;
        this.priority = priority;
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

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
