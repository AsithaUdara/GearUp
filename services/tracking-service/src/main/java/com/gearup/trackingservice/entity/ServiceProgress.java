package com.gearup.trackingservice.entity;

import java.time.LocalDate;
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
@Table(name = "service_progress", indexes = {
    @Index(name = "idx_service_progress_customer", columnList = "customer_name"),
    @Index(name = "idx_service_progress_service_id", columnList = "service_id"),
    @Index(name = "idx_service_progress_technician", columnList = "technician_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "progress_id", unique = true, nullable = false, length = 50)
    private String progressId;
    
    @Column(name = "service_id", nullable = false, length = 50)
    private String serviceId;
    
    @Column(name = "vehicle_model", nullable = false, length = 100)
    private String vehicleModel;
    
    @Column(name = "vehicle_year", length = 10)
    private String vehicleYear;
    
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    @Column(name = "customer_email", length = 100)
    private String customerEmail;
    
    @Column(name = "appointment_date")
    private LocalDate appointmentDate;
    
    @Column(name = "appointment_time", length = 20)
    private String appointmentTime;
    
    @Column(name = "estimated_completion", length = 50)
    private String estimatedCompletion;
    
    @Builder.Default
    @Column(name = "current_step")
    private Integer currentStep = 0;
    
    @Builder.Default
    @Column(name = "total_steps")
    private Integer totalSteps = 5;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", length = 20)
    private OverallStatus overallStatus;
    
    @Column(name = "technician_id", length = 100)
    private String technicianId;
    
    @Column(name = "technician_name", length = 100)
    private String technicianName;
    
    @Column(name = "location_name", length = 200)
    private String locationName;
    
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum OverallStatus {
        scheduled, in_progress, completed, delayed
    }
}
