package com.gearup.appointmentservice.dto;

import com.gearup.appointmentservice.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long timeSlotId;
    private String userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BookingStatus status;
    private String notes;
    private LocalDateTime bookingDate;
    private TimeSlotDTO timeSlot;

    private Long assignedEmployeeId;
    private String assignedEmployeeName;
}