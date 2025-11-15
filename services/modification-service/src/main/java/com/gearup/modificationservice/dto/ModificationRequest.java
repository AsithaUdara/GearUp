package com.gearup.modificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModificationRequest {
    
    private String userId;  // Can come from header or body
    
    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;
    
    private String vehicleLabel;  // Optional: "2025 Toyota GR Hilux — CBE-2938"
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Message is required")
    private String message;
}
