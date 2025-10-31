package com.gearup.notificationservice.dto.event;

import com.gearup.shared.event.BaseNotificationEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskAssignedEvent extends BaseNotificationEvent {
    private String taskId;
    private String taskTitle;
    private String assignedBy;
    private String dueDate;
    private String priority;

    @java.lang.SuppressWarnings(value = "all")
    @lombok.Generated
    public static class TaskAssignedEventBuilder {

        public Object eventId(String toString) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
