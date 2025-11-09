package com.gearup.appointmentservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.appointmentservice.dto.ServiceDTO;
import com.gearup.appointmentservice.service.AppointmentServiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/services")
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