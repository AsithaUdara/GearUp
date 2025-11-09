package com.gearup.customerservice.config;

import com.gearup.security.FirebaseAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
@EnableMethodSecurity
@ConditionalOnWebApplication
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   Optional<FirebaseAuthenticationFilter> firebaseAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/admin/**").authenticated()
                .anyRequest().permitAll()
            );

        // Register the filter only if a FirebaseAuthenticationFilter bean is available.
        firebaseAuthenticationFilter.ifPresent(filter ->
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        );

        return http.build();
    }
}
