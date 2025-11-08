package com.gearup.trackingservice.entity;

import java.math.BigDecimal;
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
@Table(name = "parts_request", indexes = {
    @Index(name = "idx_parts_request_vehicle", columnList = "vehicle"),
    @Index(name = "idx_parts_request_service_id", columnList = "service_id"),
    @Index(name = "idx_parts_request_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartsRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", unique = true, nullable = false, length = 50)
    private String requestId;
    
    @Column(nullable = false, length = 200)
    private String material;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(length = 100)
    private String vehicle;
    
    @Column(name = "service_id", length = 50)
    private String serviceId;
    
    @Column(name = "requested_by", length = 100)
    private String requestedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum RequestStatus {
        Pending, Approved, Rejected
    }
}
