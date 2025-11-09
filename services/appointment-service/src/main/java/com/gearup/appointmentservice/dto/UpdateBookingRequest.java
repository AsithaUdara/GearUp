package com.gearup.appointmentservice.dto;

import com.gearup.appointmentservice.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequest {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String notes;
    private BookingStatus status;
}