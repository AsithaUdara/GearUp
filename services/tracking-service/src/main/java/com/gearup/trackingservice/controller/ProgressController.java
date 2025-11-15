package com.gearup.trackingservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.trackingservice.dto.response.ProgressResponse;
import com.gearup.trackingservice.service.ProgressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/tracking/progress")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {
    
    private final ProgressService progressService;
    
    /**
     * Get service progress for customer
     * GET /api/tracking/progress/service/{serviceId}
     */
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ProgressResponse> getServiceProgress(@PathVariable String serviceId) {
        log.info("Fetching service progress: {}", serviceId);
        ProgressResponse response = progressService.getServiceProgress(serviceId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update service progress
     * PUT /api/tracking/progress/service/{serviceId}
     */
    @PutMapping("/service/{serviceId}")
    public ResponseEntity<ProgressResponse> updateProgress(
            @PathVariable String serviceId,
            @RequestParam int currentStep) {
        log.info("Updating service progress: {} to step: {}", serviceId, currentStep);
        ProgressResponse response = progressService.updateProgress(serviceId, currentStep);
        return ResponseEntity.ok(response);
    }
}
