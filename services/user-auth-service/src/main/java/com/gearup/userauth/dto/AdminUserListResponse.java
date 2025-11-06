package com.gearup.userauth.dto;

import com.gearup.userauth.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {
    
    private Long id;
    private String email;
    private String name; // Combined firstName + lastName or displayName
    private String role; // Primary role name
    private String status; // Active/Deactivated
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    public static AdminUserListResponse fromUser(User user) {
        // Get name (prefer displayName, fallback to firstName + lastName)
        String name = user.getDisplayName();
        if (name == null || name.isEmpty()) {
            name = (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                   (user.getLastName() != null ? user.getLastName() : "");
            name = name.trim();
        }
        
        // Get primary role (first role if multiple, or "No Role" if none)
        String role = user.getRoles().isEmpty() ? "No Role" :
                user.getRoles().stream()
                        .findFirst()
                        .map(r -> r.getName())
                        .orElse("No Role");
        
        // Map account status
        String status = user.getAccountStatus() == User.AccountStatus.ACTIVE ? "Active" : "Deactivated";
        
        return AdminUserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(name)
                .role(role)
                .status(status)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
    
    public static List<AdminUserListResponse> fromUsers(List<User> users) {
        return users.stream()
                .map(AdminUserListResponse::fromUser)
                .collect(Collectors.toList());
    }
}
