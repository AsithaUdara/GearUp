package com.gearup.appointmentservice.controller;

import com.gearup.appointmentservice.dto.ServiceDTO;
import com.gearup.appointmentservice.service.AppointmentServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ServiceController {
    
    private final AppointmentServiceService appointmentServiceService;
    
    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        log.info("GET /api/services - Fetching all available services");
        List<ServiceDTO> services = appointmentServiceService.getAllActiveServices();
        return ResponseEntity.ok(services);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long id) {
        log.info("GET /api/services/{} - Fetching service by id", id);
        return appointmentServiceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}