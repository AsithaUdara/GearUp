package com.gearup.userauth.dto;

import com.gearup.userauth.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firebaseUid;
    private String email;
    private String displayName;
    private String phoneNumber;
    private String photoUrl;
    private String accountStatus;
    private Set<RoleResponse> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static UserResponse fromUser(User user) {
        // Normalize display name: prefer stored displayName if non-blank and not a duplicated first+first
        String rawDisplay = user.getDisplayName();
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String normalized;
        if (rawDisplay == null || rawDisplay.isBlank()) {
            // Build from first/last; if last blank or duplicate, use only first
            if (last.isBlank() || first.equalsIgnoreCase(last)) {
                normalized = first;
            } else {
                normalized = (first + " " + last).trim();
            }
        } else {
            String[] tokens = rawDisplay.trim().split("\\s+");
            if (tokens.length == 2 && tokens[0].equalsIgnoreCase(tokens[1])) {
                normalized = tokens[0];
            } else {
                normalized = rawDisplay.trim();
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .displayName(normalized)
                .phoneNumber(user.getPhoneNumber())
                .photoUrl(user.getPhotoUrl())
                .accountStatus(user.getAccountStatus().name())
                .roles(user.getRoles().stream()
                        .map(RoleResponse::fromRole)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
