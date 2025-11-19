package com.gearup.trackingservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.shared.event.tracking.*;
import com.gearup.shared.messaging.EventPublisher;
import com.gearup.shared.messaging.RabbitMQConstants;
import com.gearup.trackingservice.dto.request.CreateTaskRequest;
import com.gearup.trackingservice.dto.request.UpdateTaskRequest;
import com.gearup.trackingservice.dto.response.TaskListResponse;
import com.gearup.trackingservice.dto.response.TaskResponse;
import com.gearup.trackingservice.entity.WorkTask;
import com.gearup.trackingservice.exception.TaskNotFoundException;
import com.gearup.trackingservice.repository.WorkTaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final WorkTaskRepository workTaskRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        log.info("Creating new task for employee: {}", request.getAssigneeId());
        
        String taskId = generateTaskId();
        
        WorkTask task = WorkTask.builder()
            .taskId(taskId)
            .serviceId(request.getServiceId())
            .vehicle(request.getVehicle())
            .customer(request.getCustomer())
            .serviceType(request.getServiceType())
            .assigneeId(request.getAssigneeId())
            .status(WorkTask.TaskStatus.pending)
            .progressStep(1)
            .estimatedDuration(request.getEstimatedDuration())
            .notes(request.getNotes())
            .time(getCurrentTime())
            .build();
            
        WorkTask saved = workTaskRepository.save(task);
        
        // 📢 Publish TaskCreatedEvent
        try {
            TaskCreatedEvent event = new TaskCreatedEvent(
                UUID.randomUUID().toString(),
                taskId,
                request.getAssigneeId(),
                request.getServiceType(),
                request.getCustomer(),
                request.getVehicle(),
                request.getEstimatedDuration(),
                LocalDateTime.now(),
                request.getNotes()
            );
            eventPublisher.publish(
                RabbitMQConstants.TRACKING_EXCHANGE,
                RabbitMQConstants.TASK_CREATED_KEY,
                event
            );
            log.info("📢 Published TaskCreatedEvent for task: {}", taskId);
        } catch (Exception e) {
            log.error("❌ Failed to publish TaskCreatedEvent for task: {}", taskId, e);
        }
        
        log.info("Task created successfully: {}", taskId);
        
        return mapToResponse(saved);
    }
    
    @Transactional
    public TaskResponse updateTaskProgress(String taskId, UpdateTaskRequest request) {
        log.info("Updating task progress: {}", taskId);
        
        WorkTask task = workTaskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        
        WorkTask.TaskStatus oldStatus = task.getStatus();
        Integer oldProgressStep = task.getProgressStep();
            
        task.setStatus(request.getStatus());
        task.setProgressStep(request.getProgressStep());
        task.setNotes(request.getNotes());
        task.setActualDuration(request.getActualDuration());
        
        if (request.getStatus() == WorkTask.TaskStatus.completed) {
            task.setCompletedAt(LocalDateTime.now());
            task.setProgressStep(5);
            log.info("Task marked as completed: {}", taskId);
            
            // 📢 Publish TaskCompletedEvent
            try {
                TaskCompletedEvent event = new TaskCompletedEvent(
                    UUID.randomUUID().toString(),
                    taskId,
                    task.getAssigneeId(),
                    task.getServiceType(),
                    task.getAssigneeId()  // completedBy
                );
                eventPublisher.publish(
                    RabbitMQConstants.TRACKING_EXCHANGE,
                    RabbitMQConstants.TASK_COMPLETED_KEY,
                    event
                );
                log.info("📢 Published TaskCompletedEvent for task: {}", taskId);
            } catch (Exception e) {
                log.error("❌ Failed to publish TaskCompletedEvent for task: {}", taskId, e);
            }
        } else if (oldStatus == WorkTask.TaskStatus.pending && request.getStatus() == WorkTask.TaskStatus.in_progress) {
            // 📢 Publish TaskStartedEvent
            try {
                TaskStartedEvent event = new TaskStartedEvent(
                    UUID.randomUUID().toString(),
                    taskId,
                    task.getAssigneeId(),
                    LocalDateTime.now(),
                    task.getServiceType(),
                    task.getCustomer(),
                    request.getProgressStep()
                );
                eventPublisher.publish(
                    RabbitMQConstants.TRACKING_EXCHANGE,
                    RabbitMQConstants.TASK_STARTED_KEY,
                    event
                );
                log.info("📢 Published TaskStartedEvent for task: {}", taskId);
            } catch (Exception e) {
                log.error("❌ Failed to publish TaskStartedEvent for task: {}", taskId, e);
            }
        } else if (oldProgressStep != null && !oldProgressStep.equals(request.getProgressStep())) {
            // 📢 Publish TaskProgressUpdatedEvent
            try {
                Integer percentComplete = (request.getProgressStep() * 100) / 5;  // Calculate percentage
                TaskProgressUpdatedEvent event = new TaskProgressUpdatedEvent(
                    UUID.randomUUID().toString(),
                    taskId,
                    task.getAssigneeId(),
                    request.getProgressStep(),
                    request.getStatus().name(),
                    LocalDateTime.now(),
                    request.getNotes(),
                    percentComplete
                );
                eventPublisher.publish(
                    RabbitMQConstants.TRACKING_EXCHANGE,
                    RabbitMQConstants.TASK_PROGRESS_UPDATED_KEY,
                    event
                );
                log.info("📢 Published TaskProgressUpdatedEvent for task: {}", taskId);
            } catch (Exception e) {
                log.error("❌ Failed to publish TaskProgressUpdatedEvent for task: {}", taskId, e);
            }
        }
        
        WorkTask updated = workTaskRepository.save(task);
        
        return mapToResponse(updated);
    }
    
    @Transactional(readOnly = true)
    public TaskListResponse getEmployeeTasks(String employeeId) {
        log.info("Fetching tasks for employee: {}", employeeId);
        
        var current = workTaskRepository.findFirstByAssigneeIdAndStatusOrderByCreatedAtAsc(
            employeeId, WorkTask.TaskStatus.in_progress
        );
        
        // Fetch pending tasks
        var pendingTasks = workTaskRepository.findByAssigneeIdAndStatus(
            employeeId, WorkTask.TaskStatus.pending
        );
        log.info("Found {} pending tasks for employee {}", pendingTasks.size(), employeeId);
        
        // Fetch all in_progress tasks (in work tasks) from work_task table
        var inWorkTasks = workTaskRepository.findByAssigneeIdAndStatus(
            employeeId, WorkTask.TaskStatus.in_progress
        );
        log.info("Found {} in_progress tasks for employee {}", inWorkTasks.size(), employeeId);
        
        // Sort in work tasks by created date descending
        inWorkTasks.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Combine pending and in_progress tasks for assigned tasks
        var assigned = new ArrayList<WorkTask>();
        assigned.addAll(pendingTasks);
        assigned.addAll(inWorkTasks);
        // Sort by created date descending
        assigned.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        log.info("Total assigned tasks (pending + in_progress): {}", assigned.size());
        
        var completed = workTaskRepository.findByAssigneeIdAndStatusAndCompletedAtAfterOrderByCompletedAtDesc(
            employeeId, 
            WorkTask.TaskStatus.completed,
            LocalDateTime.now().toLocalDate().atStartOfDay()
        );
        log.info("Found {} completed tasks for employee {}", completed.size(), employeeId);
        
        long inProgressCount = workTaskRepository.countByAssigneeIdAndStatus(
            employeeId, WorkTask.TaskStatus.in_progress
        );
        
        var response = TaskListResponse.builder()
            .currentTask(current.map(this::mapToResponse).orElse(null))
            .assignedTasks(assigned.stream().map(this::mapToResponse).collect(Collectors.toList()))
            .inWorkTasks(inWorkTasks.stream().map(this::mapToResponse).collect(Collectors.toList()))
            .completedTasks(completed.stream().map(this::mapToResponse).collect(Collectors.toList()))
            .totalAssigned(assigned.size())
            .totalInProgress((int) inProgressCount)
            .totalCompleted(completed.size())
            .build();
        
        log.info("Returning response with {} assigned tasks, {} in work tasks, {} completed tasks", 
            response.getAssignedTasks().size(), 
            response.getInWorkTasks().size(), 
            response.getCompletedTasks().size());
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(String taskId) {
        log.info("Fetching task: {}", taskId);
        
        WorkTask task = workTaskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
            
        return mapToResponse(task);
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByVehicle(String vehicle) {
        log.info("Fetching tasks for vehicle: {}", vehicle);
        
        return workTaskRepository.findByVehicle(vehicle)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByServiceId(String serviceId) {
        log.info("Fetching tasks for service: {}", serviceId);
        
        return workTaskRepository.findByServiceId(serviceId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    private String generateTaskId() {
        return "task-" + System.currentTimeMillis();
    }
    
    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalDateTime.now().format(formatter);
    }
    
    private TaskResponse mapToResponse(WorkTask task) {
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
