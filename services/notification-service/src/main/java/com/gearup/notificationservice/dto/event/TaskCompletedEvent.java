package com.gearup.notificationservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskCompletedEvent extends BaseNotificationEvent {
    private String taskId;
    private String taskTitle;
    private String completedBy;
}
