package com.gearup.trackingservice.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_task", indexes = {
    @Index(name = "idx_work_task_assignee", columnList = "assignee_id"),
    @Index(name = "idx_work_task_status", columnList = "status"),
    @Index(name = "idx_work_task_vehicle", columnList = "vehicle"),
    @Index(name = "idx_work_task_service_id", columnList = "service_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", unique = true, nullable = false, length = 50)
    private String taskId;
    
    @Column(name = "service_id", nullable = false, length = 50)
    private String serviceId;
    
    @Column(nullable = false, length = 100)
    private String vehicle;
    
    @Column(nullable = false, length = 100)
    private String customer;
    
    @Column(name = "service_type", nullable = false, length = 100)
    private String serviceType;
    
    @Column(name = "assignee_id", nullable = false, length = 100)
    private String assigneeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;
    
    @Column(name = "progress_step")
    private Integer progressStep;  // 1-5
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(length = 20)
    private String time;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
    
    @Column(name = "actual_duration")
    private Integer actualDuration;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public enum TaskStatus {
        pending, in_progress, completed
    }
}
