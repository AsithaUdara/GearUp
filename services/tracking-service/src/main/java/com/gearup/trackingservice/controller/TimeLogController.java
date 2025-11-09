package com.gearup.trackingservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.trackingservice.dto.request.StartTimeLogRequest;
import com.gearup.trackingservice.dto.request.StopTimeLogRequest;
import com.gearup.trackingservice.dto.response.TimeLogResponse;
import com.gearup.trackingservice.service.TimeLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class TimeLogController {
    
    private final TimeLogService timeLogService;
    
    /**
     * Start time logging for a task
     * POST /api/tracking/tasks/{taskId}/time-logs/start
     */
    @PostMapping("/tasks/{taskId}/time-logs/start")
    public ResponseEntity<TimeLogResponse> startTimeLog(
            @PathVariable String taskId,
            @RequestParam String employeeId,
            @Valid @RequestBody(required = false) StartTimeLogRequest request) {
        log.info("Starting time log for task: {} by employee: {}", taskId, employeeId);
        TimeLogResponse response = timeLogService.startTimeLog(taskId, employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Stop time logging for a task
     * POST /api/tracking/tasks/{taskId}/time-logs/stop
     */
    @PostMapping("/tasks/{taskId}/time-logs/stop")
    public ResponseEntity<TimeLogResponse> stopTimeLog(
            @PathVariable String taskId,
            @RequestParam String employeeId,
            @Valid @RequestBody(required = false) StopTimeLogRequest request) {
        log.info("Stopping time log for task: {} by employee: {}", taskId, employeeId);
        TimeLogResponse response = timeLogService.stopTimeLog(taskId, employeeId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all time logs for a task
     * GET /api/tracking/tasks/{taskId}/time-logs
     */
    @GetMapping("/tasks/{taskId}/time-logs")
    public ResponseEntity<List<TimeLogResponse>> getTimeLogsByTask(@PathVariable String taskId) {
        log.info("Fetching time logs for task: {}", taskId);
        List<TimeLogResponse> timeLogs = timeLogService.getTimeLogsByTask(taskId);
        return ResponseEntity.ok(timeLogs);
    }
    
    /**
     * Get active time log for a task and employee
     * GET /api/tracking/tasks/{taskId}/time-logs/active
     */
    @GetMapping("/tasks/{taskId}/time-logs/active")
    public ResponseEntity<TimeLogResponse> getActiveTimeLog(
            @PathVariable String taskId,
            @RequestParam String employeeId) {
        log.info("Fetching active time log for task: {} and employee: {}", taskId, employeeId);
        TimeLogResponse timeLog = timeLogService.getActiveTimeLog(taskId, employeeId);
        if (timeLog == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(timeLog);
    }
    
    /**
     * Get all time logs for an employee
     * GET /api/tracking/employees/{employeeId}/time-logs
     */
    @GetMapping("/employees/{employeeId}/time-logs")
    public ResponseEntity<List<TimeLogResponse>> getTimeLogsByEmployee(@PathVariable String employeeId) {
        log.info("Fetching time logs for employee: {}", employeeId);
        List<TimeLogResponse> timeLogs = timeLogService.getTimeLogsByEmployee(employeeId);
        return ResponseEntity.ok(timeLogs);
    }
}

