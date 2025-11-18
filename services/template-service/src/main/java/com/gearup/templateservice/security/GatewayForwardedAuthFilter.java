package com.gearup.templateservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Simple auth filter that trusts the API Gateway forwarded headers (X-User-Id or X-Forwarded-Uid)
 * and establishes a Spring Security Authentication so that requestMatchers("/api/v1/admin/**")
 * recognize the request as authenticated. This avoids re-validating Firebase tokens inside each
 * downstream service and resolves 401 responses when the gateway already performed verification.
 */
public class GatewayForwardedAuthFilter extends OncePerRequestFilter {

    // No-arg constructor; this filter trusts forwarded headers only and does not perform any
    // development fallback. Authorities are granted only when `X-User-Roles` is present.
    public GatewayForwardedAuthFilter() {
        // No-op constructor: behavior is controlled by presence of forwarded headers only.
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only set authentication if not already present
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String uid = request.getHeader("X-Forwarded-Uid");
            if (uid == null || uid.isBlank()) {
                uid = request.getHeader("X-User-Id");
            }
            if (uid != null && !uid.isBlank()) {
                // Assign ROLE_ADMIN if header X-User-Roles contains ADMIN (comma separated) else anonymous authorities
                String rolesHeader = request.getHeader("X-User-Roles");
                java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                if (rolesHeader != null) {
                    for (String r : rolesHeader.split(",")) {
                        String role = r.trim();
                        if (!role.isEmpty()) {
                            // Spring expects ROLE_ prefix in hasRole() checks
                            if (!role.startsWith("ROLE_")) {
                                role = "ROLE_" + role;
                            }
                            authorities.add(new SimpleGrantedAuthority(role));
                        }
                    }
                }
                // No fallback: if no rolesHeader present, do not add authorities here.
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(uid, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                // Provide firebaseUid attribute compatibility for existing controllers
                request.setAttribute("firebaseUid", uid);
            }
        }

        filterChain.doFilter(request, response);
    }
}