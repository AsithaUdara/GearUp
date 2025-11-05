package com.gearup.automobileservice.controller;

import com.gearup.automobileservice.dto.BookingResponse;
import com.gearup.automobileservice.dto.CreateBookingRequest;
import com.gearup.automobileservice.service.VehicleBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class VehicleBookingController {
    
    private final VehicleBookingService bookingService;
    
    /**
     * Create a new vehicle booking
     * This will:
     * 1. Save booking to PostgreSQL
     * 2. Cache booking in Redis
     * 3. Publish VehicleBookingCreatedEvent to RabbitMQ
     * 4. Notification-service will consume the event and create a notification
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        log.info("POST /api/bookings - Creating booking for user: {}", request.getUserId());
        
        BookingResponse response = bookingService.createBooking(request);
        
        log.info("Booking created successfully: {}", response.getBookingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Quick test endpoint to create a sample booking with mock data
     * This makes it easy to test the cross-service communication flow
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> createTestBooking(
            @RequestParam(defaultValue = "test-user-123") String userId) {
        
        log.info("POST /api/bookings/test - Creating test booking for user: {}", userId);
        
        // Create mock booking request
        CreateBookingRequest request = CreateBookingRequest.builder()
                .vehicleId("VEH-" + UUID.randomUUID().toString().substring(0, 8))
                .vehicleName("Tesla Model 3")
                .userId(userId)
                .customerName("John Doe")
                .bookingStartDate(LocalDateTime.now().plusDays(1))
                .bookingEndDate(LocalDateTime.now().plusDays(3))
                .totalAmount(new BigDecimal("299.99"))
                .build();
        
        BookingResponse booking = bookingService.createBooking(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Test booking created successfully");
        response.put("booking", booking);
        response.put("steps", List.of(
                "✓ Booking saved to PostgreSQL database",
                "✓ Booking cached in Redis",
                "✓ VehicleBookingCreatedEvent published to RabbitMQ",
                "→ Notification-service will consume event",
                "→ Notification will be created in notification database",
                "→ Check notification-service logs to verify event consumption"
        ));
        response.put("nextSteps", Map.of(
                "checkNotifications", "GET http://localhost:8081/api/notifications/user/" + userId,
                "confirmBooking", "POST http://localhost:8080/api/bookings/" + booking.getBookingId() + "/confirm",
                "getBooking", "GET http://localhost:8080/api/bookings/" + booking.getBookingId()
        ));
        
        log.info("Test booking created: {}", booking.getBookingId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Confirm a booking
     * This will:
     * 1. Update booking status in database
     * 2. Update cache in Redis
     * 3. Publish VehicleBookingConfirmedEvent to RabbitMQ
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable String bookingId) {
        log.info("POST /api/bookings/{}/confirm - Confirming booking", bookingId);
        
        BookingResponse response = bookingService.confirmBooking(bookingId);
        
        log.info("Booking confirmed successfully: {}", bookingId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get booking by ID (uses Redis cache)
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        log.info("GET /api/bookings/{} - Fetching booking (Redis cache enabled)", bookingId);
        
        BookingResponse response = bookingService.getBooking(bookingId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all bookings for a user (uses Redis cache)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable String userId) {
        log.info("GET /api/bookings/user/{} - Fetching user bookings (Redis cache enabled)", userId);
        
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        
        log.info("Found {} bookings for user: {}", bookings.size(), userId);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Get all bookings (no caching)
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        log.info("GET /api/bookings - Fetching all bookings");
        
        List<BookingResponse> bookings = bookingService.getAllBookings();
        
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Cancel a booking (evicts cache)
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable String bookingId) {
        log.info("DELETE /api/bookings/{} - Cancelling booking", bookingId);
        
        bookingService.cancelBooking(bookingId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Booking cancelled successfully");
        response.put("bookingId", bookingId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "automobile-service");
        health.put("features", List.of(
                "PostgreSQL database integration",
                "Redis caching enabled",
                "RabbitMQ event publishing",
                "Eureka service registration"
        ));
        
        return ResponseEntity.ok(health);
    }
}
