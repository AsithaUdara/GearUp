package com.gearup.modificationservice.controller;

import com.gearup.modificationservice.dto.ModificationRequestDTO;
import com.gearup.modificationservice.dto.ModificationServiceDTO;
import com.gearup.modificationservice.service.ModificationRequestService;
import com.gearup.modificationservice.service.ModificationServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-modifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ServiceModificationController {
    
    private final ModificationServiceService modificationServiceService;
    private final ModificationRequestService modificationRequestService;
    
    // GET /api/service-modifications - List all services
    @GetMapping
    public ResponseEntity<List<ModificationServiceDTO>> getAllServices() {
        log.info("GET /api/service-modifications - Fetching all active services");
        List<ModificationServiceDTO> services = modificationServiceService.getAllActiveServices();
        return ResponseEntity.ok(services);
    }
    
    // GET /api/service-modifications/:serviceId
    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceModificationResponse> getServiceWithModifications(@PathVariable Long serviceId) {
        log.info("GET /api/service-modifications/{} - Fetching service with all modification data", serviceId);
        
        ModificationServiceDTO service = modificationServiceService.getServiceById(serviceId)
                .orElse(null);
        
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<ModificationRequestDTO> requests = modificationRequestService.getRequestsByServiceId(serviceId);
        
        ServiceModificationResponse response = new ServiceModificationResponse(service, requests);
        return ResponseEntity.ok(response);
    }
    
    // GET /api/service-modifications/:serviceId/refresh
    @GetMapping("/{serviceId}/refresh")
    public ResponseEntity<ModificationServiceDTO> refreshServiceInfo(@PathVariable Long serviceId) {
        log.info("GET /api/service-modifications/{}/refresh - Refreshing latest service info", serviceId);
        return modificationServiceService.getServiceById(serviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Inner class for response
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ServiceModificationResponse {
        private ModificationServiceDTO service;
        private List<ModificationRequestDTO> modificationRequests;
    }
}