package com.gearup.notificationservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.notificationservice.simulator.EventSimulatorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for simulating events to test event-driven communication.
 * FOR TESTING/DEVELOPMENT PURPOSES ONLY.
 */
@RestController
@RequestMapping("/api/events/simulate")
@RequiredArgsConstructor
@Slf4j
public class EventSimulatorController {

    private final EventSimulatorService simulatorService;

    @PostMapping("/vehicle-booking-created")
    public ResponseEntity<Map<String, String>> simulateVehicleBookingCreated(@RequestParam String userId) {
        log.info("Simulating vehicle booking created event for user: {}", userId);
        String eventId = simulatorService.simulateVehicleBookingCreated(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("message", "Vehicle booking created event published successfully");
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vehicle-booking-confirmed")
    public ResponseEntity<Map<String, String>> simulateVehicleBookingConfirmed(@RequestParam String userId) {
        log.info("Simulating vehicle booking confirmed event for user: {}", userId);
        String eventId = simulatorService.simulateVehicleBookingConfirmed(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("message", "Vehicle booking confirmed event published successfully");
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vehicle-maintenance-scheduled")
    public ResponseEntity<Map<String, String>> simulateVehicleMaintenanceScheduled(@RequestParam String userId) {
        log.info("Simulating vehicle maintenance scheduled event for user: {}", userId);
        String eventId = simulatorService.simulateVehicleMaintenanceScheduled(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("message", "Vehicle maintenance scheduled event published successfully");
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invoice-created")
    public ResponseEntity<Map<String, String>> simulateInvoiceCreated(@RequestParam String userId) {
        log.info("Simulating invoice created event for user: {}", userId);
        String eventId = simulatorService.simulateInvoiceCreated(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("message", "Invoice created event published successfully");
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invoice-paid")
    public ResponseEntity<Map<String, String>> simulateInvoicePaid(@RequestParam String userId) {
        log.info("Simulating invoice paid event for user: {}", userId);
        String eventId = simulatorService.simulateInvoicePaid(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("message", "Invoice paid event published successfully");
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/task-assigned")
    public ResponseEntity<Map<String, String>> simulateTaskAssigned(@RequestParam String userId) {
        log.info("Simulating task assigned event for user: {}", userId);
        String eventId = simulatorService.simulateTaskAssigned(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("message", "Task assigned event published successfully");
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/task-completed")
    public ResponseEntity<Map<String, String>> simulateTaskCompleted(@RequestParam String userId) {
        log.info("Simulating task completed event for user: {}", userId);
        String eventId = simulatorService.simulateTaskCompleted(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("message", "Task completed event published successfully");
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/all")
    public ResponseEntity<Map<String, String>> simulateAllEvents(@RequestParam String userId) {
        log.info("Simulating all event types for user: {}", userId);
        simulatorService.simulateAllEvents(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All events published successfully");
        response.put("userId", userId);
        response.put("eventsPublished", "7");
        
        return ResponseEntity.ok(response);
    }
}
