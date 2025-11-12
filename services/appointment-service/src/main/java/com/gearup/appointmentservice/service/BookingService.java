package com.gearup.appointmentservice.service;

import com.gearup.appointmentservice.dto.*;
import com.gearup.appointmentservice.entity.Booking;
import com.gearup.appointmentservice.entity.BookingStatus;
import com.gearup.appointmentservice.entity.Service;
import com.gearup.appointmentservice.entity.TimeSlot;
import com.gearup.appointmentservice.repository.BookingRepository;
import com.gearup.appointmentservice.repository.ServiceRepository;
import com.gearup.appointmentservice.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final TimeSlotRepository timeSlotRepository;
    
    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        log.debug("Creating booking for user: {} and service: {}", request.getUserId(), request.getServiceId());
        
        // Validate service exists
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        // Validate time slot exists and is available
        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
        
        if (!timeSlot.getIsAvailable()) {
            throw new RuntimeException("Time slot is not available");
        }
        
        // Check if time slot is already booked
        if (bookingRepository.existsByTimeSlotIdAndStatusNotCancelled(request.getTimeSlotId())) {
            throw new RuntimeException("Time slot is already booked");
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setService(service);
        booking.setTimeSlot(timeSlot);
        booking.setUserId(request.getUserId());
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setNotes(request.getNotes());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
        
        booking = bookingRepository.save(booking);
        
        // Mark time slot as unavailable
        timeSlot.setIsAvailable(false);
        timeSlotRepository.save(timeSlot);
        
        log.info("Booking created successfully with id: {}", booking.getId());
        return convertToDTO(booking);
    }
    
    public List<BookingDTO> getUserBookings(String userId) {
        log.debug("Fetching bookings for user: {}", userId);
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingDateDesc(userId);
        return bookings.stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    public Optional<BookingDTO> getBookingById(Long id) {
        log.debug("Fetching booking with id: {}", id);
        return bookingRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional
    public BookingDTO updateBooking(Long id, UpdateBookingRequest request) {
        log.debug("Updating booking with id: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Update fields if provided
        if (request.getCustomerName() != null) {
            booking.setCustomerName(request.getCustomerName());
        }
        if (request.getCustomerEmail() != null) {
            booking.setCustomerEmail(request.getCustomerEmail());
        }
        if (request.getCustomerPhone() != null) {
            booking.setCustomerPhone(request.getCustomerPhone());
        }
        if (request.getNotes() != null) {
            booking.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            BookingStatus oldStatus = booking.getStatus();
            booking.setStatus(request.getStatus());
            
            // If booking is cancelled, make time slot available again
            if (request.getStatus() == BookingStatus.CANCELLED && oldStatus != BookingStatus.CANCELLED) {
                TimeSlot timeSlot = booking.getTimeSlot();
                timeSlot.setIsAvailable(true);
                timeSlotRepository.save(timeSlot);
            }
        }
        
        booking = bookingRepository.save(booking);
        log.info("Booking updated successfully with id: {}", booking.getId());
        return convertToDTO(booking);
    }
    
    @Transactional
    public void cancelBooking(Long id) {
        log.debug("Cancelling booking with id: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // Make time slot available again
        TimeSlot timeSlot = booking.getTimeSlot();
        timeSlot.setIsAvailable(true);
        timeSlotRepository.save(timeSlot);
        
        log.info("Booking cancelled successfully with id: {}", booking.getId());
    }
    
    private BookingDTO convertToDTO(Booking booking) {
        TimeSlotDTO timeSlotDTO = new TimeSlotDTO(
                booking.getTimeSlot().getId(),
                booking.getTimeSlot().getService().getId(),
                booking.getTimeSlot().getService().getName(),
                booking.getTimeSlot().getSlotDate(),
                booking.getTimeSlot().getStartTime(),
                booking.getTimeSlot().getEndTime(),
                booking.getTimeSlot().getIsAvailable()
        );
        
        return new BookingDTO(
                booking.getId(),
                booking.getService().getId(),
                booking.getService().getName(),
                booking.getTimeSlot().getId(),
                booking.getUserId(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                booking.getCustomerPhone(),
                booking.getStatus(),
                booking.getNotes(),
                booking.getBookingDate(),
                timeSlotDTO
        );
    }
}