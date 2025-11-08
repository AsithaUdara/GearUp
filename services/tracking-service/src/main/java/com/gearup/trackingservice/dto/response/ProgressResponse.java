package com.gearup.trackingservice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gearup.trackingservice.entity.ServiceProgress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    
    private Long id;
    private String progressId;
    private String serviceId;
    private String vehicleModel;
    private String vehicleYear;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String estimatedCompletion;
    private Integer currentStep;
    private Integer totalSteps;
    private ServiceProgress.OverallStatus overallStatus;
    private String technicianId;
    private String technicianName;
    private String locationName;
    private LocalDateTime lastUpdate;
    private Integer progressPercentage;
}
