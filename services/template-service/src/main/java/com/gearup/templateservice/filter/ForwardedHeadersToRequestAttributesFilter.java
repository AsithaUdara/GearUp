package com.gearup.templateservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Maps incoming forwarded headers (used by api-gateway in dev) into request attributes
 * so controllers relying on @RequestAttribute("firebaseUid") continue to work.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ForwardedHeadersToRequestAttributesFilter extends OncePerRequestFilter {

    public static final String HEADER_FORWARDED_UID = "X-Forwarded-Uid";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String ATTR_FIREBASE_UID = "firebaseUid";
    public static final String ATTR_USER_ROLES = "userRoles";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // if request attribute already set (by internal code) do not override
        Object existingUid = request.getAttribute(ATTR_FIREBASE_UID);
        if (existingUid == null) {
            String forwardedUid = request.getHeader(HEADER_FORWARDED_UID);
            if (forwardedUid != null && !forwardedUid.isBlank()) {
                request.setAttribute(ATTR_FIREBASE_UID, forwardedUid);
            }
        }

        // also map user roles header into a request attribute for downstream use if needed
        Object existingRoles = request.getAttribute(ATTR_USER_ROLES);
        if (existingRoles == null) {
            String roles = request.getHeader(HEADER_USER_ROLES);
            if (roles != null && !roles.isBlank()) {
                request.setAttribute(ATTR_USER_ROLES, roles);
            }
        }

        filterChain.doFilter(request, response);
    }
}
