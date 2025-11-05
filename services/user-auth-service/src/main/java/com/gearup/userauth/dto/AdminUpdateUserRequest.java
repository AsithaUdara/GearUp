package com.gearup.userauth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {
    
    @NotNull(message = "Role is required")
    @Pattern(regexp = "ADMIN|EMPLOYEE|CUSTOMER", message = "Role must be ADMIN, EMPLOYEE, or CUSTOMER")
    private String role;
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "Active|Deactivated", message = "Status must be Active or Deactivated")
    private String status;
}
