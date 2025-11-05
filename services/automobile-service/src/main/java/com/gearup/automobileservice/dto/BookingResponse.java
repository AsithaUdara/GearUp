package com.gearup.automobileservice.dto;

import com.gearup.automobileservice.entity.VehicleBooking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse implements Serializable {
    
    private String bookingId;
    private String vehicleId;
    private String vehicleName;
    private String userId;
    private String customerName;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    private BigDecimal totalAmount;
    private VehicleBooking.BookingStatus status;
    private LocalDateTime createdAt;
    
    public static BookingResponse from(VehicleBooking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .vehicleId(booking.getVehicleId())
                .vehicleName(booking.getVehicleName())
                .userId(booking.getUserId())
                .customerName(booking.getCustomerName())
                .bookingStartDate(booking.getBookingStartDate())
                .bookingEndDate(booking.getBookingEndDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
