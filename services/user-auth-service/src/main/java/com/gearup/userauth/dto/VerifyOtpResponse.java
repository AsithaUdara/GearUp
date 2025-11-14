package com.gearup.userauth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpResponse {
    private boolean requirePasswordChange;
    private String passwordChangeToken;
    private long expiresIn; // seconds
}
