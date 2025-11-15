package com.gearup.modificationservice.service;

import com.gearup.modificationservice.dto.ModificationRequest;
import com.gearup.modificationservice.dto.ModificationResponse;
import com.gearup.modificationservice.entity.Modification;
import com.gearup.modificationservice.repository.ModificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModificationService {
    
    private final ModificationRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "modification.exchange";
    private static final Set<String> VALID_STATUSES = Set.of("pending", "approved", "in_progress", "completed", "rejected");
    
    @Transactional
    public ModificationResponse createModification(ModificationRequest request) {
        Modification modification = new Modification();
        modification.setUserId(request.getUserId());
        modification.setVehicleId(request.getVehicleId());
        modification.setVehicleLabel(request.getVehicleLabel());
        modification.setSubject(request.getSubject());
        modification.setMessage(request.getMessage());
        modification.setStatus("pending");
        
        Modification saved = repository.save(modification);
        log.info("Created modification request: {} for user: {}", saved.getId(), saved.getUserId());
        
        publishEvent("modification.created", saved);
        
        return toResponse(saved);
    }
    
    public List<ModificationResponse> getByUserId(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<ModificationResponse> getByVehicleId(String vehicleId) {
        return repository.findByVehicleIdOrderByCreatedAtDesc(vehicleId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<ModificationResponse> getById(UUID id) {
        return repository.findById(id).map(this::toResponse);
    }
    
    @Transactional
    public ModificationResponse updateStatus(UUID id, String newStatus) {
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus + ". Must be one of: " + VALID_STATUSES);
        }
        
        Modification modification = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Modification not found: " + id));
        
        String oldStatus = modification.getStatus();
        modification.setStatus(newStatus);
        Modification updated = repository.save(modification);
        
        log.info("Updated modification {} status: {} -> {}", id, oldStatus, newStatus);
        
        publishEvent("modification.status.changed", updated);
        
        return toResponse(updated);
    }
    
    @Transactional
    public void deleteModification(UUID id) {
        Modification modification = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Modification not found: " + id));
        
        repository.delete(modification);
        log.info("Deleted modification: {}", id);
        
        publishEvent("modification.deleted", modification);
    }
    
    private ModificationResponse toResponse(Modification mod) {
        return ModificationResponse.builder()
                .id(mod.getId().toString())
                .userId(mod.getUserId())
                .vehicleId(mod.getVehicleId())
                .vehicleLabel(mod.getVehicleLabel())
                .subject(mod.getSubject())
                .message(mod.getMessage())
                .status(mod.getStatus())
                .createdAt(mod.getCreatedAt())
                .updatedAt(mod.getUpdatedAt())
                .build();
    }
    
    private void publishEvent(String routingKey, Modification mod) {
        try {
            Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "modificationId", Objects.toString(mod.getId(), ""),
                "userId", Objects.toString(mod.getUserId(), ""),
                "vehicleId", Objects.toString(mod.getVehicleId(), ""),
                "status", Objects.toString(mod.getStatus(), ""),
                "subject", Objects.toString(mod.getSubject(), ""),
                "timestamp", Instant.now().toString()
            );
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
            log.debug("Published event [{}]: {}", routingKey, payload);
        } catch (Exception e) {
            log.warn("Failed to publish event [{}]: {}", routingKey, e.getMessage());
            // Dev-mode resilience: don't fail the request if broker transiently unavailable
        }
    }
}
