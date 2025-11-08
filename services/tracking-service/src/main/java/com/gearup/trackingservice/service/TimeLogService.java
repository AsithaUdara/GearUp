package com.gearup.trackingservice.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.trackingservice.dto.request.StartTimeLogRequest;
import com.gearup.trackingservice.dto.request.StopTimeLogRequest;
import com.gearup.trackingservice.dto.response.TimeLogResponse;
import com.gearup.trackingservice.entity.TimeLog;
import com.gearup.trackingservice.entity.WorkTask;
import com.gearup.trackingservice.exception.TaskNotFoundException;
import com.gearup.trackingservice.repository.TimeLogRepository;
import com.gearup.trackingservice.repository.WorkTaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeLogService {
    
    private final TimeLogRepository timeLogRepository;
    private final WorkTaskRepository workTaskRepository;
    
    /**
     * Start time logging for a task
     * If there's an active time log, it will be cancelled first
     */
    @Transactional
    public TimeLogResponse startTimeLog(String taskId, String employeeId, StartTimeLogRequest request) {
        log.info("Starting time log for task: {} by employee: {}", taskId, employeeId);
        
        // Find the task
        WorkTask task = workTaskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        
        // Check if there's an active time log for this task and employee
        timeLogRepository.findFirstByTaskAndEmployeeIdAndStatusOrderByStartTimeDesc(
            task, employeeId, TimeLog.TimeLogStatus.active
        ).ifPresent(activeLog -> {
            log.info("Cancelling existing active time log: {}", activeLog.getLogId());
            activeLog.setStatus(TimeLog.TimeLogStatus.cancelled);
            activeLog.setStopTime(LocalDateTime.now());
            timeLogRepository.save(activeLog);
        });
        
        // Create new time log
        String logId = generateLogId();
        TimeLog timeLog = TimeLog.builder()
            .logId(logId)
            .task(task)
            .employeeId(employeeId)
            .status(TimeLog.TimeLogStatus.active)
            .startTime(LocalDateTime.now())
            .notes(request != null ? request.getNotes() : null)
            .build();
        
        TimeLog saved = timeLogRepository.save(timeLog);
        
        // Update task status to in_progress if it's pending
        if (task.getStatus() == WorkTask.TaskStatus.pending) {
            task.setStatus(WorkTask.TaskStatus.in_progress);
            workTaskRepository.save(task);
        }
        
        log.info("Time log started successfully: {}", logId);
        
        return mapToResponse(saved);
    }
    
    /**
     * Stop time logging for a task
     */
    @Transactional
    public TimeLogResponse stopTimeLog(String taskId, String employeeId, StopTimeLogRequest request) {
        log.info("Stopping time log for task: {} by employee: {}", taskId, employeeId);
        
        // Find the task
        WorkTask task = workTaskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        
        // Find active time log
        TimeLog activeLog = timeLogRepository.findFirstByTaskAndEmployeeIdAndStatusOrderByStartTimeDesc(
            task, employeeId, TimeLog.TimeLogStatus.active
        ).orElseThrow(() -> new RuntimeException("No active time log found for task: " + taskId));
        
        // Calculate duration
        LocalDateTime stopTime = LocalDateTime.now();
        Duration duration = Duration.between(activeLog.getStartTime(), stopTime);
        int durationMinutes = (int) duration.toMinutes();
        
        // Update time log
        activeLog.setStatus(TimeLog.TimeLogStatus.completed);
        activeLog.setStopTime(stopTime);
        activeLog.setDurationMinutes(durationMinutes);
        if (request != null && request.getNotes() != null) {
            activeLog.setNotes(request.getNotes());
        }
        
        TimeLog saved = timeLogRepository.save(activeLog);
        
        // Update task's actualDuration by summing all completed time logs
        updateTaskActualDuration(taskId);
        
        log.info("Time log stopped successfully: {}, duration: {} minutes", activeLog.getLogId(), durationMinutes);
        
        return mapToResponse(saved);
    }
    
    /**
     * Get all time logs for a task
     */
    @Transactional(readOnly = true)
    public List<TimeLogResponse> getTimeLogsByTask(String taskId) {
        log.info("Fetching time logs for task: {}", taskId);
        
        return timeLogRepository.findByTask_TaskIdOrderByStartTimeDesc(taskId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all time logs for an employee
     */
    @Transactional(readOnly = true)
    public List<TimeLogResponse> getTimeLogsByEmployee(String employeeId) {
        log.info("Fetching time logs for employee: {}", employeeId);
        
        return timeLogRepository.findByEmployeeIdOrderByStartTimeDesc(employeeId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get active time log for a task and employee
     */
    @Transactional(readOnly = true)
    public TimeLogResponse getActiveTimeLog(String taskId, String employeeId) {
        log.info("Fetching active time log for task: {} and employee: {}", taskId, employeeId);
        
        WorkTask task = workTaskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        
        return timeLogRepository.findFirstByTaskAndEmployeeIdAndStatusOrderByStartTimeDesc(
            task, employeeId, TimeLog.TimeLogStatus.active
        ).map(this::mapToResponse)
        .orElse(null);
    }
    
    /**
     * Update task's actualDuration by summing all completed time logs
     */
    @Transactional
    public void updateTaskActualDuration(String taskId) {
        Integer totalMinutes = timeLogRepository.sumDurationByTaskId(taskId);
        
        WorkTask task = workTaskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
        
        task.setActualDuration(totalMinutes);
        workTaskRepository.save(task);
        
        log.info("Updated task {} actualDuration to {} minutes", taskId, totalMinutes);
    }
    
    private String generateLogId() {
        return "log-" + System.currentTimeMillis();
    }
    
    private TimeLogResponse mapToResponse(TimeLog timeLog) {
        return TimeLogResponse.builder()
            .id(timeLog.getId())
            .logId(timeLog.getLogId())
            .taskId(timeLog.getTask().getTaskId())
            .employeeId(timeLog.getEmployeeId())
            .status(timeLog.getStatus().name())
            .startTime(timeLog.getStartTime())
            .stopTime(timeLog.getStopTime())
            .durationMinutes(timeLog.getDurationMinutes())
            .notes(timeLog.getNotes())
            .createdAt(timeLog.getCreatedAt())
            .updatedAt(timeLog.getUpdatedAt())
            .build();
    }
}

