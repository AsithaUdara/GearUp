package com.gearup.vehicleservice.service;

import com.gearup.shared.event.vehicle.*;
import com.gearup.shared.messaging.EventPublisher;
import com.gearup.shared.messaging.RabbitMQConstants;
import com.gearup.vehicleservice.domain.Vehicle;
import com.gearup.vehicleservice.domain.VehicleStatus;
import com.gearup.vehicleservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository repository;
    private final EventPublisher eventPublisher;

    public List<Vehicle> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    public Optional<Vehicle> getById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public Vehicle create(Vehicle v) {
        Vehicle saved = repository.save(v);
        publishEvent("vehicle.created", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "vehicleId", saved.getId().toString(),
                "userId", Objects.toString(saved.getUserId(), ""),
                "make", Objects.toString(saved.getMake(), ""),
                "model", Objects.toString(saved.getModel(), ""),
                "year", saved.getYear(),
                "numberPlate", Objects.toString(saved.getNumberPlate(), ""),
                "photoURL", Objects.toString(saved.getPhotoURL(), ""),
                "timestamp", Instant.now().toString()
        ));
        
        // 📢 Publish VehicleRegisteredEvent
        try {
            VehicleRegisteredEvent event = new VehicleRegisteredEvent(
                UUID.randomUUID().toString(),
                saved.getId().getMostSignificantBits(),  // Convert UUID to Long
                saved.getUserId(),
                saved.getMake(),
                saved.getModel(),
                saved.getYear(),
                saved.getNumberPlate(),
                null,  // VIN not available
                LocalDateTime.now()
            );
            eventPublisher.publish(
                RabbitMQConstants.VEHICLE_EXCHANGE,
                RabbitMQConstants.VEHICLE_CREATED_KEY,
                event
            );
            log.info("📢 Published VehicleRegisteredEvent for vehicle: {}", saved.getId());
        } catch (Exception e) {
            log.error("❌ Failed to publish VehicleRegisteredEvent for vehicle: {}", saved.getId(), e);
        }
        
        return saved;
    }

    @Transactional
    public Vehicle update(UUID id, Vehicle incoming) {
        Vehicle existing = repository.findById(id).orElseThrow();
        existing.setMake(incoming.getMake());
        existing.setModel(incoming.getModel());
        existing.setYear(incoming.getYear());
        existing.setNumberPlate(incoming.getNumberPlate());
        existing.setPhotoURL(incoming.getPhotoURL());
        Vehicle saved = repository.save(existing);
        publishEvent("vehicle.updated", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "vehicleId", saved.getId().toString(),
                "userId", Objects.toString(saved.getUserId(), ""),
                "timestamp", Instant.now().toString()
        ));
        
        // 📢 Publish VehicleUpdatedEvent
        try {
            VehicleUpdatedEvent event = new VehicleUpdatedEvent(
                UUID.randomUUID().toString(),
                saved.getId().getMostSignificantBits(),  // Convert UUID to Long
                saved.getUserId(),
                "profile",  // updatedField
                null,  // oldValue
                "updated",  // newValue
                LocalDateTime.now(),
                "SYSTEM"  // updatedBy
            );
            eventPublisher.publish(
                RabbitMQConstants.VEHICLE_EXCHANGE,
                RabbitMQConstants.VEHICLE_UPDATED_KEY,
                event
            );
            log.info("📢 Published VehicleUpdatedEvent for vehicle: {}", saved.getId());
        } catch (Exception e) {
            log.error("❌ Failed to publish VehicleUpdatedEvent for vehicle: {}", saved.getId(), e);
        }
        
        return saved;
    }

    @Transactional
    public Vehicle changeStatus(UUID id, VehicleStatus newStatus) {
        Vehicle existing = repository.findById(id).orElseThrow();
        VehicleStatus old = existing.getStatus();
        existing.setStatus(newStatus);
        Vehicle saved = repository.save(existing);
        publishEvent("vehicle.status.changed", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "vehicleId", saved.getId().toString(),
                "userId", Objects.toString(saved.getUserId(), ""),
                "oldStatus", old.name(),
                "newStatus", newStatus.name(),
                "timestamp", Instant.now().toString()
        ));
        
        log.info("Vehicle status changed from {} to {} for vehicle: {}", old, newStatus, id);
        
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private void publishEvent(String routingKey, Map<String, Object> payload) {
        try {
            eventPublisher.publish(RabbitMQConstants.VEHICLE_EXCHANGE, routingKey, payload);
            log.debug("Published event [{}]: {}", routingKey, payload);
        } catch (Exception e) {
            log.warn("Failed to publish event [{}]: {}", routingKey, e.getMessage());
        }
    }
}
