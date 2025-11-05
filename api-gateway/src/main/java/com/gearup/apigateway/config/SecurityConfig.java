package com.gearup.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Minimal reactive security configuration: disable default login page and
 * let the Gateway's authentication filter handle auth. Permit actuator endpoints.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable()
                        .authorizeExchange(ex -> ex
                                        .pathMatchers("/actuator/**").permitAll()
                                        .anyExchange().permitAll()
                        )
                        .httpBasic(basic -> basic.disable()
                                .formLogin(login -> login.disable())));

        return http.build();
    }
}
