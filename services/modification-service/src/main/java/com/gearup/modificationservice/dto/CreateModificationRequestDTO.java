package com.gearup.modificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateModificationRequestDTO {
    
    @NotNull(message = "Service ID is required")
    private Long serviceId;
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    
    private String preferredDate; // Changed from LocalDate to String
    private String notes;
}