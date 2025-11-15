package com.gearup.apigateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Minimal reactive security configuration: disable default login page and
 * let the Gateway's authentication filter handle auth. Permit actuator endpoints
 * and public endpoints like chatbot.
 * 
 * TODO: Add proper role-based access control (RBAC) for admin endpoints
 * TODO: Implement CSRF protection for state-changing operations
 * TODO: Add security headers (HSTS, X-Content-Type-Options, X-Frame-Options, CSP)
 * TODO: Configure proper CORS based on environment (production vs dev)
 * TODO: Add request size limits to prevent DoS attacks
 * TODO: Implement API versioning strategy and deprecation headers
 * TODO: Add OAuth2 resource server configuration for multi-provider support
 * TODO: Implement secure session management with Redis
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(ex -> ex
                        // Public endpoints - no authentication required
                        .pathMatchers("/api/chat/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        // All other endpoints - authentication handled by FirebaseGatewayFilter
                        .anyExchange().permitAll()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(login -> login.disable());

        return http.build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
