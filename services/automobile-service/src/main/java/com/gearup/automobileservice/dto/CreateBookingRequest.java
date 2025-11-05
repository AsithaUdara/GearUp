package com.gearup.automobileservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    private String vehicleId;
    private String vehicleName;
    private String userId;
    private String customerName;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    private BigDecimal totalAmount;
}
