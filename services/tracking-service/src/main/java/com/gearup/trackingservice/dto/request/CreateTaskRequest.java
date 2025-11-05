package com.gearup.trackingservice.dto.request;

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
public class CreateTaskRequest {
    
    @NotBlank(message = "Service ID is required")
    private String serviceId;
    
    @NotBlank(message = "Vehicle is required")
    private String vehicle;
    
    @NotBlank(message = "Customer is required")
    private String customer;
    
    @NotBlank(message = "Service type is required")
    private String serviceType;
    
    @NotBlank(message = "Assignee ID is required")
    private String assigneeId;
    
    private Integer estimatedDuration;
    
    private String notes;
}
