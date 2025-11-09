package com.gearup.appointmentservice.controller;

import com.gearup.appointmentservice.dto.TimeSlotDTO;
import com.gearup.appointmentservice.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    
    @PostMapping
    public ResponseEntity<TimeSlotDTO> createTimeSlot(@RequestBody TimeSlotDTO timeSlotDTO) {
        log.info("POST /api/timeslots - Creating time slot for service: {} on date: {}", 
                timeSlotDTO.getServiceId(), timeSlotDTO.getSlotDate());
        try {
            TimeSlotDTO created = timeSlotService.createTimeSlot(timeSlotDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.error("Error creating time slot: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<TimeSlotDTO> updateTimeSlotAvailability(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> update) {
        
        Boolean isAvailable = update.get("isAvailable");
        log.info("PATCH /api/timeslots/{} - Updating availability to: {}", id, isAvailable);
        
        try {
            TimeSlotDTO updated = timeSlotService.updateAvailability(id, isAvailable);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating time slot: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Long id) {
        log.info("DELETE /api/timeslots/{} - Deleting time slot", id);
        try {
            timeSlotService.deleteTimeSlot(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting time slot: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/block-range")
    public ResponseEntity<Void> blockDateRange(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("POST /api/timeslots/block-range - Blocking slots for service: {} from {} to {}", 
                serviceId, startDate, endDate);
        
        try {
            timeSlotService.blockDateRange(serviceId, startDate, endDate);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error blocking date range: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/unblock-range")
    public ResponseEntity<Void> unblockDateRange(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("POST /api/timeslots/unblock-range - Unblocking slots for service: {} from {} to {}", 
                serviceId, startDate, endDate);
        
        try {
            timeSlotService.unblockDateRange(serviceId, startDate, endDate);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error unblocking date range: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}