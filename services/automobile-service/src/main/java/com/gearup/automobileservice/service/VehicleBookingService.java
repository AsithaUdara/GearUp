package com.gearup.automobileservice.service;

import com.gearup.automobileservice.dto.BookingResponse;
import com.gearup.automobileservice.dto.CreateBookingRequest;
import com.gearup.automobileservice.entity.VehicleBooking;
import com.gearup.automobileservice.repository.VehicleBookingRepository;
import com.gearup.shared.event.VehicleBookingConfirmedEvent;
import com.gearup.shared.event.VehicleBookingCreatedEvent;
import com.gearup.shared.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleBookingService {
    
    private final VehicleBookingRepository bookingRepository;
    private final EventPublisher eventPublisher;
    
    /**
     * Create a new vehicle booking and publish event to RabbitMQ
     * Uses Redis to cache the created booking
     */
    @Transactional
    @CachePut(value = "bookings", key = "#result.bookingId")
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating new booking for user: {}, vehicle: {}", request.getUserId(), request.getVehicleId());
        
        // Create booking entity
        VehicleBooking booking = VehicleBooking.builder()
                .vehicleId(request.getVehicleId())
                .vehicleName(request.getVehicleName())
                .userId(request.getUserId())
                .customerName(request.getCustomerName())
                .bookingStartDate(request.getBookingStartDate())
                .bookingEndDate(request.getBookingEndDate())
                .totalAmount(request.getTotalAmount())
                .status(VehicleBooking.BookingStatus.PENDING)
                .build();
        
        // Save to database
        VehicleBooking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with ID: {}", savedBooking.getId());
        
        // Publish event to RabbitMQ for notification-service
        VehicleBookingCreatedEvent event = new VehicleBookingCreatedEvent(
                UUID.randomUUID().toString(),
                savedBooking.getUserId(),
                LocalDateTime.now(),
                savedBooking.getId(),
                savedBooking.getVehicleId(),
                savedBooking.getVehicleName(),
                savedBooking.getCustomerName(),
                savedBooking.getBookingStartDate(),
                savedBooking.getBookingEndDate(),
                savedBooking.getTotalAmount().doubleValue() // Convert BigDecimal to Double
        );
        
        eventPublisher.publishVehicleBookingCreated(event);
        log.info("Published VehicleBookingCreatedEvent for booking: {}", savedBooking.getId());
        
        return BookingResponse.from(savedBooking);
    }
    
    /**
     * Confirm a booking and publish confirmation event
     * Updates cache with confirmed booking
     */
    @Transactional
    @CachePut(value = "bookings", key = "#bookingId")
    public BookingResponse confirmBooking(String bookingId) {
        log.info("Confirming booking: {}", bookingId);
        
        VehicleBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        booking.setStatus(VehicleBooking.BookingStatus.CONFIRMED);
        VehicleBooking confirmedBooking = bookingRepository.save(booking);
        
        // Publish confirmation event
        String confirmationNumber = "CONF-" + confirmedBooking.getId().substring(0, 8).toUpperCase();
        VehicleBookingConfirmedEvent event = new VehicleBookingConfirmedEvent(
                UUID.randomUUID().toString(),
                confirmedBooking.getUserId(),
                LocalDateTime.now(),
                confirmedBooking.getId(),
                confirmedBooking.getVehicleName(),
                confirmationNumber,
                confirmedBooking.getBookingStartDate(),
                "Main Office - 123 Auto Street"
        );
        
        eventPublisher.publishVehicleBookingConfirmed(event);
        log.info("Published VehicleBookingConfirmedEvent for booking: {}", confirmedBooking.getId());
        
        return BookingResponse.from(confirmedBooking);
    }
    
    /**
     * Get booking by ID from cache (Redis) if available, otherwise from database
     */
    @Cacheable(value = "bookings", key = "#bookingId")
    public BookingResponse getBooking(String bookingId) {
        log.info("Fetching booking: {} (will check cache first)", bookingId);
        
        VehicleBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        log.info("Booking fetched from database (cache miss): {}", bookingId);
        return BookingResponse.from(booking);
    }
    
    /**
     * Get all bookings for a user (cached in Redis)
     */
    @Cacheable(value = "userBookings", key = "#userId")
    public List<BookingResponse> getUserBookings(String userId) {
        log.info("Fetching bookings for user: {} (will check cache first)", userId);
        
        List<VehicleBooking> bookings = bookingRepository.findByUserId(userId);
        log.info("Found {} bookings for user: {}", bookings.size(), userId);
        
        return bookings.stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Cancel booking and evict from cache
     */
    @Transactional
    @CacheEvict(value = {"bookings", "userBookings"}, key = "#bookingId")
    public void cancelBooking(String bookingId) {
        log.info("Cancelling booking: {}", bookingId);
        
        VehicleBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        
        booking.setStatus(VehicleBooking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        log.info("Booking cancelled and cache evicted: {}", bookingId);
    }
    
    /**
     * Get all bookings (no caching for admin view)
     */
    public List<BookingResponse> getAllBookings() {
        log.info("Fetching all bookings");
        
        return bookingRepository.findAll().stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }
}
