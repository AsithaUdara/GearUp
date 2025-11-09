package com.gearup.trackingservice.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResponse {
    
    private TaskResponse currentTask;
    private List<TaskResponse> assignedTasks;
    private List<TaskResponse> inWorkTasks;
    private List<TaskResponse> completedTasks;
    private int totalAssigned;
    private int totalInProgress;
    private int totalCompleted;
}
