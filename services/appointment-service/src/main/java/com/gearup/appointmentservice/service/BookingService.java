package com.gearup.appointmentservice.service;

import com.gearup.appointmentservice.dto.*;
import com.gearup.appointmentservice.entity.Booking;
import com.gearup.appointmentservice.entity.BookingStatus;
import com.gearup.appointmentservice.entity.TimeSlot;
import com.gearup.appointmentservice.repository.BookingRepository;
import com.gearup.appointmentservice.repository.EmployeeRepository;
import com.gearup.appointmentservice.repository.ServiceRepository;
import com.gearup.appointmentservice.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final EmployeeRepository employeeRepository;

    // -----------------------------
    // CREATE
    // -----------------------------
    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        log.debug("Creating booking for user: {} and service: {}", request.getUserId(), request.getServiceId());

        com.gearup.appointmentservice.entity.Service svc =
                serviceRepository.findById(request.getServiceId())
                        .orElseThrow(() -> new RuntimeException("Service not found"));

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
        
        if (!timeSlot.getIsAvailable())
            throw new RuntimeException("Time slot is not available");
        
        if (bookingRepository.existsByTimeSlotIdAndStatusNotCancelled(request.getTimeSlotId()))
            throw new RuntimeException("Time slot is already booked");

        Booking booking = new Booking();
        booking.setService(svc);
        booking.setTimeSlot(timeSlot);
        booking.setUserId(request.getUserId());
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setNotes(request.getNotes());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        timeSlot.setIsAvailable(false);
        timeSlotRepository.save(timeSlot);

        log.info("Booking created successfully with id: {}", saved.getId());
        return convertToDTO(saved);
    }

    // -----------------------------
    // READ
    // -----------------------------
    public List<BookingDTO> getAllBookings() {
        log.debug("Fetching all bookings");
        return bookingRepository.findAll().stream()
                .map(this::convertToDTO).toList();
    }

    // NEW: Get only assigned bookings
    public List<BookingDTO> getAssignedBookings() {
        log.debug("Fetching bookings with assigned employees");
        return bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getAssignedEmployeeId() != null)
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED)
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<BookingDTO> getBookingById(Long id) {
        log.debug("Fetching booking by ID: {}", id);
        return bookingRepository.findById(id).map(this::convertToDTO);
    }

    // -----------------------------
    // UPDATE
    // -----------------------------
    @Transactional
    public BookingDTO updateBooking(Long id, UpdateBookingRequest request) {
        log.debug("Updating booking with id: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (request.getCustomerName() != null)
            booking.setCustomerName(request.getCustomerName());
        if (request.getCustomerEmail() != null)
            booking.setCustomerEmail(request.getCustomerEmail());
        if (request.getCustomerPhone() != null)
            booking.setCustomerPhone(request.getCustomerPhone());
        if (request.getNotes() != null)
            booking.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            BookingStatus oldStatus = booking.getStatus();
            booking.setStatus(request.getStatus());
            if (request.getStatus() == BookingStatus.CANCELLED && oldStatus != BookingStatus.CANCELLED) {
                TimeSlot slot = booking.getTimeSlot();
                slot.setIsAvailable(true);
                timeSlotRepository.save(slot);
            }
        }
        
        Booking saved = bookingRepository.save(booking);
        log.info("Booking updated successfully with id: {}", saved.getId());
        return convertToDTO(saved);
    }

    @Transactional
    public BookingDTO approveBooking(Long id) {
        log.debug("Approving booking with id: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        
        log.info("Booking approved successfully: {}", booking.getId());
        return convertToDTO(booking);
    }

    // -----------------------------
    // ASSIGN EMPLOYEE (ENHANCED)
    // -----------------------------
    @Transactional
    public BookingDTO assignEmployee(Long bookingId, Long employeeId, String timeSlotStr) {
        log.debug("Assigning employee {} to booking {}", employeeId, bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // ✅ VALIDATION: Check for double-booking
        TimeSlot requestedSlot = booking.getTimeSlot();
        List<Booking> employeeBookings = bookingRepository.findByAssignedEmployeeId(employeeId);
        
        for (Booking existingBooking : employeeBookings) {
            // Skip the current booking and cancelled bookings
            if (existingBooking.getId().equals(bookingId)) continue;
            if (existingBooking.getStatus() == BookingStatus.CANCELLED) continue;
            
            TimeSlot existingSlot = existingBooking.getTimeSlot();
            
            // Check if same date and overlapping time
            if (isSameDate(requestedSlot.getSlotDate(), existingSlot.getSlotDate()) &&
                timeSlotsOverlap(requestedSlot.getStartTime(), requestedSlot.getEndTime(),
                                existingSlot.getStartTime(), existingSlot.getEndTime())) {
                
                String errorMsg = String.format(
                    "Employee is already assigned to booking #%d at %s - %s on %s",
                    existingBooking.getId(),
                    existingSlot.getStartTime(),
                    existingSlot.getEndTime(),
                    existingSlot.getSlotDate()
                );
                
                log.warn("⚠️  Double-booking prevented: {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }

        // If validation passes, assign the employee
        booking.setAssignedEmployeeId(employeeId);
        
        if (timeSlotStr != null && !timeSlotStr.isBlank()) {
            booking.setNotes((booking.getNotes() != null ? booking.getNotes() + " | " : "")
                    + "Reassigned slot: " + timeSlotStr);
        }
        
        Booking saved = bookingRepository.save(booking);
        log.info("✅ Employee {} successfully assigned to booking {}", employeeId, bookingId);
        return convertToDTO(saved);
    }

    // -----------------------------
    // CANCEL / DELETE
    // -----------------------------
    @Transactional
    public void cancelBooking(Long id) {
        log.debug("Cancelling booking with id: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new RuntimeException("Booking already cancelled");
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        TimeSlot slot = booking.getTimeSlot();
        slot.setIsAvailable(true);
        timeSlotRepository.save(slot);
        
        log.info("Booking {} cancelled", booking.getId());
    }

    // -----------------------------
    // HELPER METHODS
    // -----------------------------
    private boolean isSameDate(LocalDate date1, LocalDate date2) {
        return date1.equals(date2);
    }

    private boolean timeSlotsOverlap(LocalTime start1, LocalTime end1, 
                                     LocalTime start2, LocalTime end2) {
        // Slots overlap if: start1 < end2 AND start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private BookingDTO convertToDTO(Booking booking) {
        TimeSlotDTO slotDTO = new TimeSlotDTO(
                booking.getTimeSlot().getId(),
                booking.getTimeSlot().getService().getId(),
                booking.getTimeSlot().getService().getName(),
                booking.getTimeSlot().getSlotDate(),
                booking.getTimeSlot().getStartTime(),
                booking.getTimeSlot().getEndTime(),
                booking.getTimeSlot().getIsAvailable()
        );

        // Get employee name if assigned
        String assignedEmployeeName = null;
        if (booking.getAssignedEmployeeId() != null) {
            assignedEmployeeName = employeeRepository.findById(booking.getAssignedEmployeeId())
                    .map(employee -> employee.getName())
                    .orElse(null);
        }

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
                slotDTO,
                booking.getAssignedEmployeeId(),
                assignedEmployeeName
        );
    }
}