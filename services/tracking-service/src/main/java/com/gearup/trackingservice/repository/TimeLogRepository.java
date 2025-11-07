package com.gearup.trackingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gearup.trackingservice.entity.TimeLog;
import com.gearup.trackingservice.entity.WorkTask;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {
    
    Optional<TimeLog> findByLogId(String logId);
    
    List<TimeLog> findByTaskOrderByStartTimeDesc(WorkTask task);
    
    List<TimeLog> findByTask_TaskIdOrderByStartTimeDesc(String taskId);
    
    List<TimeLog> findByEmployeeIdOrderByStartTimeDesc(String employeeId);
    
    Optional<TimeLog> findFirstByTaskAndEmployeeIdAndStatusOrderByStartTimeDesc(
        WorkTask task, 
        String employeeId, 
        TimeLog.TimeLogStatus status
    );
    
    @Query("SELECT COALESCE(SUM(t.durationMinutes), 0) FROM TimeLog t WHERE t.task.taskId = :taskId AND t.status = 'completed'")
    Integer sumDurationByTaskId(@Param("taskId") String taskId);
    
    List<TimeLog> findByTask_TaskIdAndStatusOrderByStartTimeDesc(String taskId, TimeLog.TimeLogStatus status);
}



