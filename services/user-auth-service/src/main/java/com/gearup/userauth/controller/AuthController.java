package com.gearup.userauth.controller;

import com.gearup.userauth.dto.*;
import com.gearup.userauth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate user with Firebase ID token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody AuthRequest request) {
        logger.info("Login request received");
        TokenResponse tokenResponse = authService.authenticateWithFirebase(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", tokenResponse));
    }

    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", tokenResponse));
    }

    /**
     * Logout user (invalidate access token)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        logger.info("Logout successful");
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * Logout from all sessions
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @RequestAttribute("userId") Long userId) {
        authService.logoutAllSessions(userId);
        logger.info("Logout from all sessions successful");
        return ResponseEntity.ok(ApiResponse.success("Logged out from all sessions", null));
    }

    /**
     * Setup password for new employee/admin accounts
     * Public endpoint - uses OTP for verification
     */
    @PostMapping("/setup-password")
    public ResponseEntity<ApiResponse<TokenResponse>> setupPassword(
            @Valid @RequestBody SetupPasswordRequest request) {
        logger.info("Password setup request received for email: {}", request.getEmail());
        TokenResponse tokenResponse = authService.setupPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password setup successful. You are now logged in.", tokenResponse));
    }

    /**
     * Verify OTP and return short-lived token for password change
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
        @Valid @RequestBody VerifyOTPRequest request) {
        logger.info("Verify OTP request for email: {}", request.getEmail());
        VerifyOtpResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified", response));
    }
}
