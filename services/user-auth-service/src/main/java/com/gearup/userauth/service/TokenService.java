package com.gearup.userauth.service;

import com.gearup.userauth.exception.AuthenticationException;
import com.gearup.userauth.model.User;
import com.gearup.userauth.model.UserSession;
import com.gearup.userauth.repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms:3600000}") // 1 hour default
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:2592000000}") // 30 days default
    private long refreshTokenExpirationMs;

    private final UserSessionRepository sessionRepository;
    private SecretKey secretKey;
    
    @Value("${jwt.password-change-expiration-seconds:900}") // 15 minutes default
    private long passwordChangeExpirationSeconds;

    public TokenService(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        logger.info("TokenService initialized with JWT secret");
    }

    @Transactional
    public Map<String, Object> generateTokens(User user, String deviceInfo, String ipAddress) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken();

        // Save session
        UserSession session = UserSession.builder()
                .user(user)
                .sessionToken(accessToken)
                .refreshToken(refreshToken)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusSeconds(accessTokenExpirationMs / 1000))
                .refreshExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .isActive(true)
                .build();

        sessionRepository.save(session);
        logger.info("Generated tokens for user: {}", user.getEmail());

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("expiresIn", accessTokenExpirationMs / 1000);
        return tokens;
    }

    private String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("firebaseUid", user.getFirebaseUid());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toArray());

        return Jwts.builder()
                .subject(user.getFirebaseUid())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateScopedToken(User user, String scope, long ttlSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttlSeconds * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("firebaseUid", user.getFirebaseUid());
        claims.put("scope", scope);

        return Jwts.builder()
                .subject(user.getFirebaseUid())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new AuthenticationException("Invalid or expired token");
        }
    }

    @Transactional
    public void invalidateSession(String sessionToken) {
        sessionRepository.deactivateSession(sessionToken);
        logger.info("Invalidated session");
    }

    @Transactional
    public void invalidateAllUserSessions(Long userId) {
        sessionRepository.deactivateAllUserSessions(userId);
        logger.info("Invalidated all sessions for user: {}", userId);
    }

    public UserSession getSessionByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));
    }

    public UserSession getSessionByToken(String token) {
        return sessionRepository.findBySessionToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid session token"));
    }
}
