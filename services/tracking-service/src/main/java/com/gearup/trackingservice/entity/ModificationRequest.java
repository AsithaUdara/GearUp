package com.gearup.trackingservice.entity;

import java.math.BigDecimal;
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
@Table(name = "modification_request", indexes = {
    @Index(name = "idx_modification_request_vehicle", columnList = "vehicle"),
    @Index(name = "idx_modification_request_status", columnList = "status"),
    @Index(name = "idx_modification_request_assigned", columnList = "assigned_to_employee_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModificationRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", unique = true, nullable = false, length = 50)
    private String requestId;
    
    @Column(name = "service_id", nullable = false, length = 50)
    private String serviceId;
    
    @Column(nullable = false, length = 100)
    private String vehicle;
    
    @Column(nullable = false, length = 100)
    private String customer;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RequestType type;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;
    
    @Column(name = "requested_by", nullable = false, length = 100)
    private String requestedBy;
    
    @Column(name = "assigned_to_employee_id", length = 100)
    private String assignedToEmployeeId;
    
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;
    
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
    
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum RequestType {
        add_service, remove_service, change_service, urgent_repair
    }
    
    public enum RequestStatus {
        pending, approved, rejected, in_progress, completed
    }
}
