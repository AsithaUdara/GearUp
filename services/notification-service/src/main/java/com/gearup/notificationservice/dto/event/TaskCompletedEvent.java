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
public class TaskCompletedEvent extends BaseNotificationEvent {
    private String taskId;
    private String taskTitle;
    private String completedBy;
}
