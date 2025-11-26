package com.gearup.userauth.service;

import com.gearup.userauth.dto.*;
import com.gearup.userauth.exception.AuthenticationException;
import com.gearup.userauth.model.User;
import com.gearup.userauth.model.UserSession;
import com.gearup.userauth.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // TODO: Add brute force protection with rate limiting per email/IP
    // TODO: Implement comprehensive audit logging for all authentication events
    // TODO: Add failed login attempt tracking and notification
    // TODO: Implement account lockout after N failed attempts
    // TODO: Add multi-factor authentication (MFA) support
    // TODO: Implement password complexity requirements and validation
    // TODO: Add password expiration and rotation policies
    // TODO: Implement concurrent session limits per user
    // TODO: Add device fingerprinting for suspicious activity detection
    // TODO: Implement token revocation list with Redis
    // TODO: Add email verification reminder for unverified accounts
    // TODO: Implement passwordless authentication options (magic links, WebAuthn)

    private final UserService userService;
    private final TokenService tokenService;
    private final AuditService auditService;
    private final OTPService otpService;
    private final UserRepository userRepository;

    public AuthService(UserService userService, TokenService tokenService, AuditService auditService,
                       OTPService otpService, UserRepository userRepository) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.auditService = auditService;
        this.otpService = otpService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public VerifyOtpResponse verifyOtp(VerifyOTPRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("User not found with email: " + request.getEmail()));

        if (!otpService.validateOtp(user, request.getOtp())) {
            throw new AuthenticationException("Invalid or expired OTP");
        }

        if (user.getIsPasswordSet() != null && user.getIsPasswordSet()) {
            // Already set, no need to change; respond with no requirement
            return VerifyOtpResponse.builder()
                    .requirePasswordChange(false)
                    .passwordChangeToken(null)
                    .expiresIn(0)
                    .build();
        }

        String token = tokenService.generateScopedToken(user, "PASSWORD_CHANGE", 900); // 15 min
        return VerifyOtpResponse.builder()
                .requirePasswordChange(true)
                .passwordChangeToken(token)
                .expiresIn(900)
                .build();
    }

    @Transactional
    public TokenResponse authenticateWithFirebase(AuthRequest authRequest) {
        try {
            // Verify Firebase token
            FirebaseToken decodedToken = FirebaseAuth.getInstance()
                    .verifyIdToken(authRequest.getFirebaseToken());
            
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // Get or create user
            User user = userService.getUserByFirebaseUid(firebaseUid);
            
            if (user == null) {
                throw new AuthenticationException("User not registered. Please register first.");
            }

            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateUserEntity(user);

            // Generate JWT tokens
            Map<String, Object> tokens = tokenService.generateTokens(
                    user, 
                    authRequest.getDeviceInfo(), 
                    authRequest.getIpAddress()
            );

            // Log audit
            auditService.logUserAction(user.getId(), "LOGIN", "User logged in successfully", null, null);

            logger.info("User authenticated successfully: {}", email);

            return TokenResponse.builder()
                    .accessToken((String) tokens.get("accessToken"))
                    .refreshToken((String) tokens.get("refreshToken"))
                    .tokenType("Bearer")
                    .expiresIn((Long) tokens.get("expiresIn"))
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (Exception e) {
            logger.error("Firebase authentication failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid Firebase token", e);
        }
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        UserSession session = tokenService.getSessionByRefreshToken(refreshToken);

        if (!session.getIsActive() || session.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("Refresh token expired or invalid");
        }

        // Invalidate old session
        tokenService.invalidateSession(session.getSessionToken());

        // Generate new tokens
        User user = session.getUser();
        Map<String, Object> tokens = tokenService.generateTokens(
                user, 
                session.getDeviceInfo(), 
                session.getIpAddress()
        );

        logger.info("Token refreshed for user: {}", user.getEmail());

        return TokenResponse.builder()
                .accessToken((String) tokens.get("accessToken"))
                .refreshToken((String) tokens.get("refreshToken"))
                .tokenType("Bearer")
                .expiresIn((Long) tokens.get("expiresIn"))
                .user(UserResponse.fromUser(user))
                .build();
    }

    @Transactional
    public void logout(String accessToken) {
        tokenService.invalidateSession(accessToken);
        logger.info("User logged out successfully");
    }

    @Transactional
    public void logoutAllSessions(Long userId) {
        tokenService.invalidateAllUserSessions(userId);
        auditService.logUserAction(userId, "LOGOUT_ALL", "All sessions invalidated", null, null);
        logger.info("All sessions logged out for user: {}", userId);
    }

    /**
     * Setup password for employee/admin accounts created by admin
     * Verifies OTP and creates Firebase account with the provided password
     */
    @Transactional
    public TokenResponse setupPassword(SetupPasswordRequest request) {
        try {
            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AuthenticationException("User not found with email: " + request.getEmail()));

            // Validate OTP
            if (!otpService.validateOtp(user, request.getOtp())) {
                throw new AuthenticationException("Invalid or expired OTP");
            }

            // Check if password is already set
            if (user.getIsPasswordSet()) {
                throw new AuthenticationException("Password already set for this account. Please use login.");
            }

            // Ensure/update Firebase account with the provided password
            UserRecord userRecord = null;
            try {
                if (user.getFirebaseUid() != null && !user.getFirebaseUid().isBlank()) {
                    userRecord = FirebaseAuth.getInstance().getUser(user.getFirebaseUid());
                }
            } catch (Exception ignored) { }

            if (userRecord == null) {
                try {
                    userRecord = FirebaseAuth.getInstance().getUserByEmail(user.getEmail());
                } catch (Exception ignored) { }
            }

            if (userRecord == null) {
                // Create fresh if not exists
                UserRecord.CreateRequest createReq = new UserRecord.CreateRequest()
                        .setEmail(user.getEmail())
                        .setPassword(request.getPassword())
                        .setEmailVerified(true)
                        .setDisplayName(user.getDisplayName())
                        .setDisabled(false);
                userRecord = FirebaseAuth.getInstance().createUser(createReq);
            } else {
                // Update existing: set password and enable
                UserRecord.UpdateRequest updateReq = new UserRecord.UpdateRequest(userRecord.getUid())
                        .setPassword(request.getPassword())
                        .setEmailVerified(true)
                        .setDisabled(false);
                userRecord = FirebaseAuth.getInstance().updateUser(updateReq);
            }

            // Update user with Firebase UID if missing
            user.setFirebaseUid(userRecord.getUid());
            user.setIsPasswordSet(true);
            user.setEmailVerified(true);
            user.setLastLoginAt(LocalDateTime.now());
            
            // Clear OTP
            otpService.clearOtp(user);
            
            userRepository.save(user);

            // Generate JWT tokens for immediate login
            Map<String, Object> tokens = tokenService.generateTokens(user, "Web", "127.0.0.1");

            // Log audit
            auditService.logUserAction(user.getId(), "PASSWORD_SETUP", 
                    "Password set up successfully for employee/admin account", null, null);

            logger.info("Password setup completed successfully for user: {}", user.getEmail());

            return TokenResponse.builder()
                    .accessToken((String) tokens.get("accessToken"))
                    .refreshToken((String) tokens.get("refreshToken"))
                    .tokenType("Bearer")
                    .expiresIn((Long) tokens.get("expiresIn"))
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (Exception e) {
            logger.error("Password setup failed: {}", e.getMessage());
            if (e instanceof AuthenticationException) {
                throw (AuthenticationException) e;
            }
            throw new AuthenticationException("Failed to setup password: " + e.getMessage(), e);
        }
    }
}
