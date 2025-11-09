package com.gearup.customerservice.config;

import com.gearup.security.FirebaseAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration that is only active for non-test, servlet web contexts.
 * @Profile("!test") ensures tests using the 'test' profile (like @ActiveProfiles("test"))
 * will not load this configuration.
 * @ConditionalOnWebApplication and @ConditionalOnClass(HttpSecurity.class) ensure
 * this class only loads when a web environment and Spring Security web classes are present.
 */
@Configuration
@EnableMethodSecurity
@Profile("!test")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(HttpSecurity.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<FirebaseAuthenticationFilter> firebaseAuthenticationFilterProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/admin/**").authenticated()
                .anyRequest().permitAll()
            );

        // Register the filter only if a FirebaseAuthenticationFilter bean is available.
        firebaseAuthenticationFilterProvider.ifAvailable(filter ->
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        );

        return http.build();
    }
}
