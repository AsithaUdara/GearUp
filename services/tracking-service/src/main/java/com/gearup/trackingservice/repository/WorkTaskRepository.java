package com.gearup.trackingservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gearup.trackingservice.entity.WorkTask;

@Repository
public interface WorkTaskRepository extends JpaRepository<WorkTask, Long> {
    
    Optional<WorkTask> findByTaskId(String taskId);
    
    List<WorkTask> findByAssigneeIdOrderByCreatedAtDesc(String assigneeId);
    
    List<WorkTask> findByAssigneeIdAndStatus(String assigneeId, WorkTask.TaskStatus status);
    
    List<WorkTask> findByVehicle(String vehicle);
    
    List<WorkTask> findByServiceId(String serviceId);
    
    // Find current in-progress task for an employee
    Optional<WorkTask> findFirstByAssigneeIdAndStatusOrderByCreatedAtAsc(String assigneeId, WorkTask.TaskStatus status);
    
    // Find all assigned tasks (pending and in_progress)
    List<WorkTask> findByAssigneeIdAndStatusInOrderByCreatedAtDesc(String assigneeId, List<WorkTask.TaskStatus> statuses);
    
    // Find completed tasks for today
    List<WorkTask> findByAssigneeIdAndStatusAndCompletedAtAfterOrderByCompletedAtDesc(String assigneeId, WorkTask.TaskStatus status, LocalDateTime startOfDay);
    
    @Query("SELECT COUNT(t) FROM WorkTask t WHERE t.assigneeId = :assigneeId AND t.status = :status")
    long countByAssigneeIdAndStatus(@Param("assigneeId") String assigneeId, @Param("status") WorkTask.TaskStatus status);
}
