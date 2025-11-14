package com.gearup.userauth.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gearup.userauth.model.User;
import com.gearup.userauth.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom Firebase authentication filter that loads user roles from database.
 * This filter verifies Firebase tokens and then loads the user's roles to set proper authorities.
 */
public class UserAuthFirebaseFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public UserAuthFirebaseFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            String uid;
            // If Firebase isn't initialized (e.g., local/dev without service key), fallback to decode JWT payload
            if (FirebaseApp.getApps().isEmpty()) {
                uid = decodeUidFromJwt(idToken);
                if (uid == null || uid.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Invalid token\"}");
                    return;
                }
            } else {
                // Verify Firebase token normally
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                uid = decodedToken.getUid();
            }

            // Load user from database to get roles
            List<GrantedAuthority> authorities = new ArrayList<>();
            User user = userRepository.findByFirebaseUid(uid).orElse(null);

            if (user != null) {
                // Convert user roles to Spring Security authorities
                // Normalize role names to uppercase to align with hasRole('ADMIN') expectations
                authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                        .collect(Collectors.toList());
            }

            // Create authentication with proper authorities
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uid, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Set firebaseUid as request attribute for controllers to access
            request.setAttribute("firebaseUid", uid);

        } catch (FirebaseAuthException e) {
            // On token verification failure return 401 JSON response
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String decodeUidFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(payloadBytes);
            if (node.hasNonNull("user_id")) return node.get("user_id").asText();
            if (node.hasNonNull("sub")) return node.get("sub").asText();
            if (node.hasNonNull("uid")) return node.get("uid").asText();
        } catch (Exception ignored) {}
        return null;
    }
}
