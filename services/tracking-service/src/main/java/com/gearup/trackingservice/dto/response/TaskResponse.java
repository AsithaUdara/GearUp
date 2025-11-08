package com.gearup.trackingservice.dto.response;

import java.time.LocalDateTime;

import com.gearup.trackingservice.entity.WorkTask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private Long id;
    private String taskId;
    private String serviceId;
    private String vehicle;
    private String customer;
    private String serviceType;
    private String assigneeId;
    private WorkTask.TaskStatus status;
    private Integer progressStep;
    private String notes;
    private String time;
    private Integer estimatedDuration;
    private Integer actualDuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
