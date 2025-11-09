package com.gearup.vehicleservice.service;

import com.gearup.vehicleservice.domain.Vehicle;
import com.gearup.vehicleservice.domain.VehicleStatus;
import com.gearup.vehicleservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository repository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "vehicle.exchange";

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
                "userId", saved.getUserId(),
                "make", saved.getMake(),
                "model", saved.getModel(),
                "year", saved.getYear(),
                "numberPlate", saved.getNumberPlate(),
                "photoURL", saved.getPhotoURL(),
                "timestamp", Instant.now().toString()
        ));
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
                "userId", saved.getUserId(),
                "timestamp", Instant.now().toString()
        ));
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
                "userId", saved.getUserId(),
                "oldStatus", old.name(),
                "newStatus", newStatus.name(),
                "timestamp", Instant.now().toString()
        ));
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private void publishEvent(String routingKey, Map<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload);
        } catch (Exception ignored) {
            // Dev-mode resilience: don't fail the request if broker transiently unavailable
        }
    }
}
