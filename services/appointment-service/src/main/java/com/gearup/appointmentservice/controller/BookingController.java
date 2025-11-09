package com.gearup.appointmentservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gearup.appointmentservice.dto.BookingDTO;
import com.gearup.appointmentservice.dto.CreateBookingRequest;
import com.gearup.appointmentservice.dto.UpdateBookingRequest;
import com.gearup.appointmentservice.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller handling CRUD operations and actions for bookings.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    // -----------------------------
    // CREATE
    // -----------------------------
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("POST /api/bookings - Creating booking for user: {}", request.getUserId());
        try {
            BookingDTO booking = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (RuntimeException e) {
            log.error("Error creating booking: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // -----------------------------
    // READ
    // -----------------------------
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        log.info("GET /api/bookings - Fetching all bookings");
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // ⚠️ IMPORTANT: This MUST come BEFORE @GetMapping("/{id}")
    @GetMapping("/assigned")
    public ResponseEntity<List<BookingDTO>> getAssignedBookings() {
        log.info("GET /api/bookings/assigned - Fetching bookings with assigned employees");
        List<BookingDTO> assignedBookings = bookingService.getAssignedBookings();
        log.info("Found {} assigned bookings", assignedBookings.size());
        return ResponseEntity.ok(assignedBookings);
    }

    // This comes AFTER /assigned because it's a generic path variable
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        log.info("GET /api/bookings/{} - Fetching booking by id", id);
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------
    // UPDATE
    // -----------------------------
    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable Long id,
            @RequestBody UpdateBookingRequest request) {
        log.info("PUT /api/bookings/{} - Updating booking", id);
        try {
            BookingDTO booking = bookingService.updateBooking(id, request);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            log.error("Error updating booking: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    // ⚠️ IMPORTANT: Specific routes like /approve come BEFORE generic /{id}
    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingDTO> approveBooking(@PathVariable Long id) {
        log.info("PUT /api/bookings/{}/approve - Approving booking", id);
        try {
            BookingDTO updated = bookingService.approveBooking(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error approving booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    // ⚠️ IMPORTANT: Specific routes like /assign come with path variable
    @PutMapping("/{id}/assign")
    public ResponseEntity<BookingDTO> assignEmployee(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            @RequestParam(required = false) String timeSlot) {

        log.info("🔵 PUT /api/bookings/{}/assign - Assigning employee {} (timeSlot={})", id, employeeId, timeSlot);
        try {
            BookingDTO updated = bookingService.assignEmployee(id, employeeId, timeSlot);
            
            log.info("✅ SUCCESS! Employee {} assigned to booking {}", employeeId, id);
            log.info("📦 Customer: {}, Service: {}", updated.getCustomerName(), updated.getServiceName());
            
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("❌ Error assigning employee {} to booking {}: {}", employeeId, id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // -----------------------------
    // DELETE / CANCEL
    // -----------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        log.info("DELETE /api/bookings/{} - Cancelling booking", id);
        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error cancelling booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}