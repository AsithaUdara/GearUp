package com.gearup.vehicleservice.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gearup.vehicleservice.domain.Vehicle;
import com.gearup.vehicleservice.domain.VehicleStatus;
import com.gearup.vehicleservice.service.VehicleService;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    // TODO: Add VIN (Vehicle Identification Number) validation and decoding
    // TODO: Implement license plate format validation per country/region
    // TODO: Add vehicle manufacturer data integration (make/model validation)
    // TODO: Implement vehicle insurance information tracking
    // TODO: Add vehicle registration expiry tracking and reminders
    // TODO: Implement vehicle maintenance schedule and history
    // TODO: Add vehicle mileage tracking and oil change reminders
    // TODO: Implement vehicle document storage (registration, insurance, inspection)
    // TODO: Add vehicle ownership transfer workflow
    // TODO: Implement vehicle recall checking via external APIs
    // TODO: Add vehicle valuation estimation
    // TODO: Implement vehicle sharing/fleet management features
    // TODO: Add vehicle telematics integration (OBD-II data)
    // TODO: Implement vehicle service history export functionality
    // TODO: Add bulk vehicle import from CSV/Excel

    private final VehicleService service;

    @GetMapping("/user/{userId}")
    public List<Vehicle> getByUser(@PathVariable("userId") String userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<Vehicle> get(@PathVariable("vehicleId") UUID vehicleId) {
        Optional<Vehicle> v = service.getById(vehicleId);
        return v.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@RequestHeader(value = "X-User-ID", required = false) String userId,
                                          @RequestBody VehicleRequest body) {
        if (userId == null || userId.isBlank()) {
            userId = body.getUserId();
        }
        Vehicle v = Vehicle.builder()
                .userId(userId)
                .make(body.getMake())
                .model(body.getModel())
                .year(parseYear(body.getYear()))
                .numberPlate(body.getNumberPlate())
                .photoURL(body.getPhotoURL())
                .build();
        Vehicle created = service.create(v);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{vehicleId}")
    public Vehicle update(@PathVariable("vehicleId") UUID vehicleId, @RequestBody VehicleRequest body) {
        Vehicle incoming = Vehicle.builder()
                .make(body.getMake())
                .model(body.getModel())
                .year(parseYear(body.getYear()))
                .numberPlate(body.getNumberPlate())
                .photoURL(body.getPhotoURL())
                .build();
        return service.update(vehicleId, incoming);
    }

    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("vehicleId") UUID vehicleId) {
        service.delete(vehicleId);
    }

    @PatchMapping("/{vehicleId}/status")
    public Vehicle changeStatus(@PathVariable("vehicleId") UUID vehicleId, @RequestParam("status") String status) {
        VehicleStatus newStatus = VehicleStatus.valueOf(status);
        return service.changeStatus(vehicleId, newStatus);
    }

    private int parseYear(String raw) {
        if (raw == null) return 0;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Data
    public static class VehicleRequest {
        @NotBlank
        private String make;
        @NotBlank
        private String model;
        // Accept both string and number; Jackson can coerce number -> string
        private String year;
        @NotBlank
        private String numberPlate;
        @JsonProperty("photoURL")
        private String photoURL;
        // Optional fallback if header missing
        private String userId;
    }
}
