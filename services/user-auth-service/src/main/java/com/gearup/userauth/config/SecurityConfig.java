package com.gearup.userauth.config;

import com.gearup.userauth.repository.UserRepository;
import com.gearup.userauth.security.UserAuthFirebaseFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// CORS is centralized at the API Gateway. Do not configure service-level CORS here to avoid
// duplicate Access-Control-Allow-Origin headers seen by the browser.

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserRepository userRepository;

    public SecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                          UserRepository userRepository) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.userRepository = userRepository;
    }

    @Bean
    public UserAuthFirebaseFilter userAuthFirebaseFilter() {
        return new UserAuthFirebaseFilter(userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // CORS is handled at the API Gateway. Disable here to avoid duplicate
            // Access-Control-Allow-Origin headers which cause browser rejections.
            .cors(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/setup-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/verify-otp").permitAll()
                // Dev-only helper endpoints (controller active only under 'dev' profile)
                .requestMatchers("/api/v1/dev/**").permitAll()
                
                // Actuator endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(customAuthenticationEntryPoint))
            .addFilterBefore(userAuthFirebaseFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // NOTE: Intentionally removed any CorsConfigurationSource bean. When present,
    // Spring WebFlux auto-configures a CorsWebFilter that adds CORS headers at the
    // service level, which combined with the API Gateway CORS results in duplicated
    // Access-Control-Allow-Origin values like "http://localhost:3000, http://localhost:3000".
}
