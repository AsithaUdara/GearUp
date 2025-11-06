package com.gearup.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Simple WebFilter that protects /actuator/** endpoints using a static token
 * supplied via `management.actuator.token` property.
 */
@Component
public class ActuatorSecurityFilter implements WebFilter {

    @Value("${management.actuator.token:}")
    private String actuatorToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // Allow chatbot endpoints without any checks
        if (path != null && path.startsWith("/api/chat")) {
            return chain.filter(exchange);
        }
        
        if (path != null && path.startsWith("/actuator")) {
            if (actuatorToken == null || actuatorToken.isBlank()) {
                // no token configured -> deny access in production scenarios
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String header = exchange.getRequest().getHeaders().getFirst("X-Actuator-Token");
            if (header == null || !header.equals(actuatorToken)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }
}
