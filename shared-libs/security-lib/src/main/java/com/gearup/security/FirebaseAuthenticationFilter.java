package com.gearup.security;

import java.io.IOException;
import java.util.ArrayList;

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
 */
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

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
            // On token verification failure return 401 JSON response
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
