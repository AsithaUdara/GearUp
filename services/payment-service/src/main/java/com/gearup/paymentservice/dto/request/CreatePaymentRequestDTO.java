package com.gearup.paymentservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequestDTO {
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name cannot exceed 255 characters")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String customerEmail;
    
    @NotBlank(message = "Vehicle information is required")
    @Size(max = 500, message = "Vehicle info cannot exceed 500 characters")
    private String vehicleInfo;
    
    @NotEmpty(message = "At least one service item is required")
    @Valid
    private List<ServiceItemDTO> services;
    
    @NotBlank(message = "Submitted by is required")
    @Size(max = 255, message = "Submitted by cannot exceed 255 characters")
    private String submittedBy;
    
    @NotNull(message = "Submitted date is required")
    private LocalDate submittedDate;
}
