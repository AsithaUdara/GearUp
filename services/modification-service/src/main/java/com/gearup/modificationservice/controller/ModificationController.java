package com.gearup.modificationservice.controller;

import com.gearup.modificationservice.dto.ModificationRequest;
import com.gearup.modificationservice.dto.ModificationResponse;
import com.gearup.modificationservice.service.ModificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/modifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ModificationController {
    
    private final ModificationService service;
    
    @PostMapping
    public ResponseEntity<?> createModification(
            @RequestHeader(value = "X-User-ID", required = false) String headerUserId,
            @Valid @RequestBody ModificationRequest request) {
        
        // Use header userId if body doesn't have it
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            if (headerUserId == null || headerUserId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "userId is required (in body or X-User-ID header)"));
            }
            request.setUserId(headerUserId);
        }
        
        try {
            ModificationResponse response = service.createModification(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating modification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create modification: " + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ModificationResponse>> getByUserId(@PathVariable("userId") String userId) {
        List<ModificationResponse> modifications = service.getByUserId(userId);
        return ResponseEntity.ok(modifications);
    }
    
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ModificationResponse>> getByVehicleId(@PathVariable("vehicleId") String vehicleId) {
        List<ModificationResponse> modifications = service.getByVehicleId(vehicleId);
        return ResponseEntity.ok(modifications);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return service.getById(uuid)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid modification ID format"));
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") String id,
            @RequestParam("status") String status) {
        
        try {
            UUID uuid = UUID.fromString(id);
            ModificationResponse response = service.updateStatus(uuid, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating modification status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update status: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModification(@PathVariable("id") String id) {
        try {
            UUID uuid = UUID.fromString(id);
            service.deleteModification(uuid);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting modification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete modification: " + e.getMessage()));
        }
    }
}
