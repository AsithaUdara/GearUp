package com.gearup.templateservice.config;

import com.gearup.templateservice.security.GatewayForwardedAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                // Enforce ADMIN role for service template management endpoints
                .requestMatchers("/api/v1/admin/service-templates/**").hasRole("ADMIN")
                // Fallback for other admin endpoints still requires authentication
                .requestMatchers("/api/v1/admin/**").authenticated()
                .anyRequest().permitAll()
            )
            // Trust gateway forwarded headers (X-User-Id) rather than verifying Firebase again
            .addFilterBefore(new GatewayForwardedAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
