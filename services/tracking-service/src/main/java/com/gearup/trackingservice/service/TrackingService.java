package com.gearup.trackingservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.trackingservice.dto.response.DailySummaryResponse;
import com.gearup.trackingservice.dto.response.TaskResponse;
import com.gearup.trackingservice.entity.WorkTask;
import com.gearup.trackingservice.repository.WorkTaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {
    
    private final WorkTaskRepository workTaskRepository;
    
    @Transactional(readOnly = true)
    public DailySummaryResponse getDailySummary(String employeeId) {
        log.info("Generating daily summary for employee: {}", employeeId);
        
        List<WorkTask> completedToday = workTaskRepository.findByAssigneeIdAndStatusAndCompletedAtAfterOrderByCompletedAtDesc(
            employeeId, 
            WorkTask.TaskStatus.completed,
            java.time.LocalDateTime.now().toLocalDate().atStartOfDay()
        );
        List<WorkTask> inProgress = workTaskRepository.findByAssigneeIdAndStatus(
            employeeId, WorkTask.TaskStatus.in_progress
        );
        
        // Group by vehicle
        Map<String, List<TaskResponse>> tasksByVehicle = new HashMap<>();
        Map<String, Integer> completedCountByVehicle = new HashMap<>();
        
        for (WorkTask task : completedToday) {
            String vehicle = task.getVehicle();
            tasksByVehicle.computeIfAbsent(vehicle, k -> new java.util.ArrayList<>())
                .add(mapToTaskResponse(task));
            completedCountByVehicle.merge(vehicle, 1, Integer::sum);
        }
        
        return DailySummaryResponse.builder()
            .totalTasksCompleted(completedToday.size())
            .totalTasksInProgress(inProgress.size())
            .totalVehiclesServiced(tasksByVehicle.size())
            .tasksByVehicle(tasksByVehicle)
            .completedCountByVehicle(completedCountByVehicle)
            .build();
    }
    
    private TaskResponse mapToTaskResponse(WorkTask task) {
        return TaskResponse.builder()
            .id(task.getId())
            .taskId(task.getTaskId())
            .serviceId(task.getServiceId())
            .vehicle(task.getVehicle())
            .customer(task.getCustomer())
            .serviceType(task.getServiceType())
            .assigneeId(task.getAssigneeId())
            .status(task.getStatus())
            .progressStep(task.getProgressStep())
            .notes(task.getNotes())
            .time(task.getTime())
            .estimatedDuration(task.getEstimatedDuration())
            .actualDuration(task.getActualDuration())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .completedAt(task.getCompletedAt())
            .build();
    }
}
