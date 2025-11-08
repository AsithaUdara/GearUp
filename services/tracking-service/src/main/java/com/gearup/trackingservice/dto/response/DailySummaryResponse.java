package com.gearup.trackingservice.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryResponse {
    
    private int totalTasksCompleted;
    private int totalTasksInProgress;
    private int totalVehiclesServiced;
    private Map<String, List<TaskResponse>> tasksByVehicle;
    private Map<String, Integer> completedCountByVehicle;
}
