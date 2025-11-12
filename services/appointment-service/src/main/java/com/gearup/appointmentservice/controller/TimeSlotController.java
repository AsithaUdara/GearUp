package com.gearup.appointmentservice.controller;

import com.gearup.appointmentservice.dto.TimeSlotDTO;
import com.gearup.appointmentservice.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TimeSlotController {
    
    private final TimeSlotService timeSlotService;
    
    @GetMapping
    public ResponseEntity<List<TimeSlotDTO>> getAvailableTimeSlots(
            @RequestParam(required = false) Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("GET /api/timeslots - Fetching available time slots for service: {} on date: {}", serviceId, date);
        List<TimeSlotDTO> timeSlots = timeSlotService.getAvailableTimeSlots(serviceId, date);
        return ResponseEntity.ok(timeSlots);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotDTO> getTimeSlotById(@PathVariable Long id) {
        log.info("GET /api/timeslots/{} - Fetching time slot by id", id);
        return timeSlotService.getTimeSlotById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}