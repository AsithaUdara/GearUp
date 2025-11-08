package com.gearup.trackingservice.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.gearup.trackingservice.entity.PartsRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartsRequest {
    
    @NotBlank(message = "Material is required")
    private String material;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotNull(message = "Status is required")
    private PartsRequest.RequestStatus status;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private String vehicle;
    
    private String serviceId;
    
    private String requestedBy;
    
    private String notes;
    
    private BigDecimal cost;
}
