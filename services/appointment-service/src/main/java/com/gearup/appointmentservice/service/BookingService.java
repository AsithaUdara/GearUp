package com.gearup.appointmentservice.service;

import com.gearup.appointmentservice.dto.*;
import com.gearup.appointmentservice.entity.Booking;
import com.gearup.appointmentservice.entity.BookingStatus;
import com.gearup.appointmentservice.entity.Employee;
import com.gearup.appointmentservice.entity.TimeSlot;
import com.gearup.appointmentservice.repository.BookingRepository;
import com.gearup.appointmentservice.repository.EmployeeRepository;
import com.gearup.appointmentservice.repository.ServiceRepository;
import com.gearup.appointmentservice.repository.TimeSlotRepository;
import com.gearup.shared.event.appointment.*;
import com.gearup.shared.messaging.EventPublisher;
import com.gearup.shared.messaging.RabbitMQConstants;
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
    private final EventPublisher eventPublisher;

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

        // 📢 Publish AppointmentCreatedEvent
        try {
            LocalDateTime slotDateTime = LocalDateTime.of(timeSlot.getSlotDate(), timeSlot.getStartTime());
            
            AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                java.util.UUID.randomUUID().toString(),  // eventId
                saved.getId(),                            // bookingId
                saved.getUserId(),                        // customerId
                saved.getCustomerName(),                  // customerName
                svc.getId(),                              // serviceId
                svc.getName(),                            // serviceName
                timeSlot.getId(),                         // timeSlotId
                slotDateTime,                             // slotDateTime
                saved.getCustomerEmail(),                 // customerEmail
                saved.getCustomerPhone(),                 // customerPhone
                saved.getStatus().name(),                 // status
                saved.getNotes()                          // notes
            );
            eventPublisher.publish(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.APPOINTMENT_CREATED_KEY,
                event
            );
            log.info("📢 Published AppointmentCreatedEvent for booking: {}", saved.getId());
        } catch (Exception e) {
            log.error("❌ Failed to publish AppointmentCreatedEvent for booking: {}", saved.getId(), e);
            // Don't fail the booking creation if event publishing fails
        }

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
        Booking saved = bookingRepository.save(booking);
        
        // 📢 Publish AppointmentApprovedEvent
        try {
            AppointmentApprovedEvent event = new AppointmentApprovedEvent(
                java.util.UUID.randomUUID().toString(),
                saved.getId(),
                saved.getUserId(),
                saved.getCustomerName(),
                saved.getService().getId(),
                saved.getService().getName(),
                "SYSTEM",  // approvedBy (can be enhanced to track actual admin)
                LocalDateTime.now()  // approvedAt
            );
            eventPublisher.publish(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.APPOINTMENT_APPROVED_KEY,
                event
            );
            log.info("📢 Published AppointmentApprovedEvent for booking: {}", saved.getId());
        } catch (Exception e) {
            log.error("❌ Failed to publish AppointmentApprovedEvent for booking: {}", saved.getId(), e);
        }
        
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
        
        // 📢 Publish EmployeeAssignedToAppointmentEvent (CRITICAL EVENT)
        try {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            TimeSlot slot = saved.getTimeSlot();
            LocalDateTime appointmentDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
            
            EmployeeAssignedToAppointmentEvent event = new EmployeeAssignedToAppointmentEvent(
                java.util.UUID.randomUUID().toString(),  // eventId
                saved.getId(),                            // bookingId
                saved.getUserId(),                        // customerId
                saved.getCustomerName(),                  // customerName
                employeeId,                               // employeeId
                employee.getName(),                       // employeeName
                employee.getEmail(),                      // employeeEmail
                saved.getService().getId(),               // serviceId
                saved.getService().getName(),             // serviceName
                appointmentDateTime,                      // appointmentDateTime
                LocalDateTime.now(),                      // assignedAt
                timeSlotStr != null ? timeSlotStr : slot.getStartTime() + "-" + slot.getEndTime()  // timeSlot
            );
            eventPublisher.publish(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.EMPLOYEE_ASSIGNED_KEY,
                event
            );
            log.info("📢 Published EmployeeAssignedToAppointmentEvent for booking: {} to employee: {}", 
                    saved.getId(), employeeId);
        } catch (Exception e) {
            log.error("❌ Failed to publish EmployeeAssignedToAppointmentEvent for booking: {}", saved.getId(), e);
            // Don't fail the assignment if event publishing fails
        }
        
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
        
        // 📢 Publish AppointmentCancelledEvent
        try {
            TimeSlot timeSlot = booking.getTimeSlot();
            
            AppointmentCancelledEvent event = new AppointmentCancelledEvent(
                java.util.UUID.randomUUID().toString(),
                booking.getId(),
                booking.getUserId(),
                booking.getCustomerName(),
                booking.getService().getId(),
                booking.getService().getName(),
                timeSlot.getId(),  // timeSlotId
                "User requested cancellation",  // cancellationReason
                LocalDateTime.now()  // cancelledAt
            );
            eventPublisher.publish(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.APPOINTMENT_CANCELLED_KEY,
                event
            );
            log.info("📢 Published AppointmentCancelledEvent for booking: {}", booking.getId());
        } catch (Exception e) {
            log.error("❌ Failed to publish AppointmentCancelledEvent for booking: {}", booking.getId(), e);
        }
        
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