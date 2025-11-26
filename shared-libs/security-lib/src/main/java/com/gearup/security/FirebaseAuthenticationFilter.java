package com.gearup.security;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shared Firebase authentication filter used across services.
 * <p>
 * This class verifies Firebase ID tokens from the Authorization header (Bearer token)
 * and sets a simple Authentication with the uid as principal. It intentionally keeps
 * GrantedAuthorities empty — services should map roles/claims as needed.
 * 
 * TODO: Extract and map custom claims to GrantedAuthorities (roles)
 * TODO: Implement token caching with Redis to reduce Firebase API calls
 * TODO: Add token blacklist/revocation list support
 * TODO: Implement token refresh logic before expiration
 * TODO: Add rate limiting per user/IP to prevent abuse
 * TODO: Implement multi-factor authentication (MFA) verification
 * TODO: Add device fingerprinting for suspicious activity detection
 * TODO: Implement audit logging for all authentication attempts
 * TODO: Add X-Request-Id header for distributed tracing
 * TODO: Implement IP whitelist/blacklist functionality
 * TODO: Add geolocation-based access restrictions
 * TODO: Implement session management and concurrent login limits
 * TODO: Add security headers to responses (HSTS, CSP, etc.)
 * TODO: Implement token expiration warning headers
 * TODO: Add comprehensive metrics for authentication events
 */
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filter for actuator endpoints and public paths
        return path.startsWith("/actuator/") || path.startsWith("/api/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = header.substring(7);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(uid, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Set firebaseUid as request attribute for controllers to access
            request.setAttribute("firebaseUid", uid);

        } catch (FirebaseAuthException e) {
            // Log exception details to help diagnose verification failures (expired, revoked, wrong project, etc.)
            log.warn("Firebase token verification failed: {}", e.getMessage());
            log.debug("Firebase token verification exception", e);
            // On token verification failure return 401 JSON response
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
