package com.gearup.trackingservice.dto.request;

import java.math.BigDecimal;

import com.gearup.trackingservice.entity.ModificationRequest;

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
public class CreateModificationRequest {
    
    @NotBlank(message = "Service ID is required")
    private String serviceId;
    
    @NotBlank(message = "Vehicle is required")
    private String vehicle;
    
    @NotBlank(message = "Customer is required")
    private String customer;
    
    @NotNull(message = "Request type is required")
    private ModificationRequest.RequestType type;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Requested by is required")
    private String requestedBy;
    
    private String assignedToEmployeeId;
    
    private BigDecimal estimatedCost;
    
    private Integer estimatedDuration;
}
