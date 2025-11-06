package com.gearup.vehicleservice.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gearup.vehicleservice.domain.Vehicle;
import com.gearup.vehicleservice.domain.VehicleStatus;
import com.gearup.vehicleservice.service.VehicleService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @GetMapping("/user/{userId}")
    public List<Vehicle> getByUser(@PathVariable String userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<Vehicle> get(@PathVariable UUID vehicleId) {
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
    public Vehicle update(@PathVariable UUID vehicleId, @RequestBody VehicleRequest body) {
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
    public void delete(@PathVariable UUID vehicleId) {
        service.delete(vehicleId);
    }

    @PatchMapping("/{vehicleId}/status")
    public Vehicle changeStatus(@PathVariable UUID vehicleId, @RequestParam("status") String status) {
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
