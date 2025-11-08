package com.gearup.modificationservice.controller;

import com.gearup.modificationservice.dto.ModificationServiceDTO;
import com.gearup.modificationservice.entity.ModificationService;
import com.gearup.modificationservice.repository.ModificationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SimpleServiceController {
    
    private final ModificationServiceRepository modificationServiceRepository;
    
    @GetMapping
    public ResponseEntity<List<ModificationServiceDTO>> getAllServices() {
        log.info("GET /api/services - Fetching all services");
        try {
            List<ModificationService> services = modificationServiceRepository.findAll();
            List<ModificationServiceDTO> dtos = services.stream()
                    .map(s -> new ModificationServiceDTO(
                            s.getId(),
                            s.getName(),
                            s.getDescription(),
                            s.getBasePrice(),
                            s.getEstimatedDurationHours(),
                            s.getIsActive()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error fetching services: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ModificationServiceDTO> getServiceById(@PathVariable Long id) {
        log.info("GET /api/services/{} - Fetching service", id);
        try {
            return modificationServiceRepository.findById(id)
                    .map(s -> new ModificationServiceDTO(
                            s.getId(),
                            s.getName(),
                            s.getDescription(),
                            s.getBasePrice(),
                            s.getEstimatedDurationHours(),
                            s.getIsActive()
                    ))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching service {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
