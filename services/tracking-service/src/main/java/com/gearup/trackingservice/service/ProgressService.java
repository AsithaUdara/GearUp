package com.gearup.trackingservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gearup.trackingservice.dto.response.ProgressResponse;
import com.gearup.trackingservice.entity.ServiceProgress;
import com.gearup.trackingservice.repository.ServiceProgressRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {
    
    private final ServiceProgressRepository serviceProgressRepository;
    
    @Transactional(readOnly = true)
    public ProgressResponse getServiceProgress(String serviceId) {
        log.info("Fetching service progress for: {}", serviceId);
        
        ServiceProgress progress = serviceProgressRepository.findByServiceId(serviceId)
            .orElseThrow(() -> new RuntimeException("Service progress not found: " + serviceId));
            
        return mapToResponse(progress);
    }
    
    @Transactional
    public ProgressResponse updateProgress(String serviceId, int currentStep) {
        log.info("Updating progress for service: {} to step: {}", serviceId, currentStep);
        
        ServiceProgress progress = serviceProgressRepository.findByServiceId(serviceId)
            .orElseThrow(() -> new RuntimeException("Service progress not found: " + serviceId));
            
        progress.setCurrentStep(currentStep);
        progress.setLastUpdate(java.time.LocalDateTime.now());
        
        // Update overall status based on progress
        if (currentStep == 0) {
            progress.setOverallStatus(ServiceProgress.OverallStatus.scheduled);
        } else if (currentStep >= progress.getTotalSteps()) {
            progress.setOverallStatus(ServiceProgress.OverallStatus.completed);
        } else {
            progress.setOverallStatus(ServiceProgress.OverallStatus.in_progress);
        }
        
        ServiceProgress updated = serviceProgressRepository.save(progress);
        
        return mapToResponse(updated);
    }
    
    private ProgressResponse mapToResponse(ServiceProgress progress) {
        int percentage = progress.getTotalSteps() > 0 
            ? (int) ((progress.getCurrentStep() * 100.0) / progress.getTotalSteps())
            : 0;
            
        return ProgressResponse.builder()
            .id(progress.getId())
            .progressId(progress.getProgressId())
            .serviceId(progress.getServiceId())
            .vehicleModel(progress.getVehicleModel())
            .vehicleYear(progress.getVehicleYear())
            .customerName(progress.getCustomerName())
            .customerPhone(progress.getCustomerPhone())
            .customerEmail(progress.getCustomerEmail())
            .appointmentDate(progress.getAppointmentDate())
            .appointmentTime(progress.getAppointmentTime())
            .estimatedCompletion(progress.getEstimatedCompletion())
            .currentStep(progress.getCurrentStep())
            .totalSteps(progress.getTotalSteps())
            .overallStatus(progress.getOverallStatus())
            .technicianId(progress.getTechnicianId())
            .technicianName(progress.getTechnicianName())
            .locationName(progress.getLocationName())
            .lastUpdate(progress.getLastUpdate())
            .progressPercentage(percentage)
            .build();
    }
}
