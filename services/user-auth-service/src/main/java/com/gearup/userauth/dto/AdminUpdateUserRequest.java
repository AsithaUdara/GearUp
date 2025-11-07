package com.gearup.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;
    
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;
    
    @NotNull(message = "Role is required")
    @Pattern(regexp = "ADMIN|EMPLOYEE|CUSTOMER", message = "Role must be ADMIN, EMPLOYEE, or CUSTOMER")
    private String role;
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "Active|Deactivated", message = "Status must be Active or Deactivated")
    private String status;
}
