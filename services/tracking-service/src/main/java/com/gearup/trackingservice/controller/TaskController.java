package com.gearup.trackingservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.trackingservice.dto.request.CreateTaskRequest;
import com.gearup.trackingservice.dto.request.UpdateTaskRequest;
import com.gearup.trackingservice.dto.response.TaskListResponse;
import com.gearup.trackingservice.dto.response.TaskResponse;
import com.gearup.trackingservice.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tracking/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * Create new task
     * POST /api/tracking/tasks
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("Creating new task for employee: {}", request.getAssigneeId());
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update task progress
     * PUT /api/tracking/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        log.info("Updating task: {}", taskId);
        TaskResponse response = taskService.updateTaskProgress(taskId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get task by ID
     * GET /api/tracking/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String taskId) {
        log.info("Fetching task: {}", taskId);
        TaskResponse response = taskService.getTaskById(taskId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get tasks by vehicle
     * GET /api/tracking/tasks/vehicle/{vehicle}
     */
    @GetMapping("/vehicle/{vehicle}")
    public ResponseEntity<List<TaskResponse>> getTasksByVehicle(@PathVariable String vehicle) {
        log.info("Fetching tasks for vehicle: {}", vehicle);
        List<TaskResponse> tasks = taskService.getTasksByVehicle(vehicle);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get tasks by service ID
     * GET /api/tracking/tasks/service/{serviceId}
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<TaskResponse>> getTasksByServiceId(@PathVariable String serviceId) {
        log.info("Fetching tasks for service: {}", serviceId);
        List<TaskResponse> tasks = taskService.getTasksByServiceId(serviceId);
        return ResponseEntity.ok(tasks);
    }
}
