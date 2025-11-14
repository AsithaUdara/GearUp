package com.gearup.appointmentservice.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.appointmentservice.dto.TimeSlotDTO;
import com.gearup.appointmentservice.service.TimeSlotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/timeslots/customer")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerTimeSlotController {
    
    private final TimeSlotService timeSlotService;
    
    // Customer-facing endpoint that excludes already booked slots
    @GetMapping
    public ResponseEntity<List<TimeSlotDTO>> getAvailableTimeSlotsForCustomer(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("GET /api/timeslots/customer - Fetching truly available time slots for customer for service: {} on date: {}", serviceId, date);
        List<TimeSlotDTO> timeSlots = timeSlotService.getAvailableTimeSlotsForCustomer(serviceId, date);
        return ResponseEntity.ok(timeSlots);
    }
}
