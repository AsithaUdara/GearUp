package com.gearup.userauth.service;

import com.gearup.userauth.dto.AuthRequest;
import com.gearup.userauth.dto.TokenResponse;
import com.gearup.userauth.dto.UserResponse;
import com.gearup.userauth.exception.AuthenticationException;
import com.gearup.userauth.model.User;
import com.gearup.userauth.model.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final TokenService tokenService;
    private final AuditService auditService;

    public AuthService(UserService userService, TokenService tokenService, AuditService auditService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.auditService = auditService;
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
}
