package com.gearup.modificationservice.controller;

import com.gearup.modificationservice.dto.CreateModificationRequestDTO;
import com.gearup.modificationservice.dto.ModificationRequestDTO;
import com.gearup.modificationservice.dto.UpdateModificationRequestDTO;
import com.gearup.modificationservice.service.ModificationRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ModificationRequestController {
    
    private final ModificationRequestService modificationRequestService;
    
    // POST /api/service-modifications/:serviceId/requests
    @PostMapping("/service-modifications/{serviceId}/requests")
    public ResponseEntity<ModificationRequestDTO> submitModificationRequest(
            @PathVariable Long serviceId,
            @Valid @RequestBody CreateModificationRequestDTO request) {
        
        log.info("POST /api/service-modifications/{}/requests - Submitting new modification request", serviceId);
        
        // Set the service ID from path parameter
        request.setServiceId(serviceId);
        
        try {
            ModificationRequestDTO createdRequest = modificationRequestService.createRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (RuntimeException e) {
            log.error("Error creating modification request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/service-modifications/:serviceId/requests
    @GetMapping("/service-modifications/{serviceId}/requests")
    public ResponseEntity<List<ModificationRequestDTO>> getAllRequestsForService(@PathVariable Long serviceId) {
        log.info("GET /api/service-modifications/{}/requests - Listing all modification requests for service", serviceId);
        List<ModificationRequestDTO> requests = modificationRequestService.getRequestsByServiceId(serviceId);
        return ResponseEntity.ok(requests);
    }
    
    // GET /api/modification-requests/:requestId
    @GetMapping("/modification-requests/{requestId}")
    public ResponseEntity<ModificationRequestDTO> getSingleRequest(@PathVariable Long requestId) {
        log.info("GET /api/modification-requests/{} - Get single modification request", requestId);
        return modificationRequestService.getRequestById(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // PATCH /api/modification-requests/:requestId
    @PatchMapping("/modification-requests/{requestId}")
    public ResponseEntity<ModificationRequestDTO> updateModificationRequest(
            @PathVariable Long requestId,
            @RequestBody UpdateModificationRequestDTO updateDTO) {
        
        log.info("PATCH /api/modification-requests/{} - Updating modification request", requestId);
        
        try {
            ModificationRequestDTO updated = modificationRequestService.updateRequest(requestId, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating modification request: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE /api/modification-requests/:requestId
    @DeleteMapping("/modification-requests/{requestId}")
    public ResponseEntity<Void> deleteModificationRequest(@PathVariable Long requestId) {
        log.info("DELETE /api/modification-requests/{} - Deleting modification request", requestId);
        
        try {
            modificationRequestService.deleteRequest(requestId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting modification request: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET /api/customers/:customerId/services
    @GetMapping("/customers/{customerId}/services")
    public ResponseEntity<List<?>> getServicesPerCustomer(@PathVariable String customerId) {
        log.info("GET /api/customers/{}/services - List all services per customer", customerId);
        
        try {
            var services = modificationRequestService.getServicesByCustomerId(customerId);
            return ResponseEntity.ok(services);
        } catch (RuntimeException e) {
            log.error("Error fetching customer services: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}