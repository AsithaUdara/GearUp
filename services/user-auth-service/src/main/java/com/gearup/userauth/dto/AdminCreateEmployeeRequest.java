package com.gearup.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateEmployeeRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Full name is required")
    private String name;
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|EMPLOYEE", message = "Role must be either ADMIN or EMPLOYEE")
    private String role;
    
    private String phoneNumber;
}
