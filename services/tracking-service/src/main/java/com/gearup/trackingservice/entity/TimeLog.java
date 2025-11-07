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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "time_log", indexes = {
    @Index(name = "idx_time_log_task", columnList = "task_id"),
    @Index(name = "idx_time_log_employee", columnList = "employee_id"),
    @Index(name = "idx_time_log_status", columnList = "status"),
    @Index(name = "idx_time_log_start_time", columnList = "start_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "log_id", unique = true, nullable = false, length = 50)
    private String logId;
    
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private WorkTask task;
    
    @Column(name = "employee_id", nullable = false, length = 100)
    private String employeeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimeLogStatus status;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "stop_time")
    private LocalDateTime stopTime;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum TimeLogStatus {
        active, completed, cancelled
    }
}



