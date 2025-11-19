package com.gearup.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOTPRequest {
	@NotBlank
	@Email
	private String email;

	@NotBlank
	private String otp;
}

