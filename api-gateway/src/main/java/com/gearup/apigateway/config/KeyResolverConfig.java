package com.gearup.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Simple KeyResolver that uses X-User-Id header if present, otherwise remote IP.
 */
@Configuration
public class KeyResolverConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                String user = exchange.getRequest().getHeaders().getFirst("X-User-Id");
                if (user != null && !user.isEmpty()) {
                    return Mono.just(user);
                }
                String ip = exchange.getRequest().getRemoteAddress() != null ?
                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
                return Mono.just(ip);
            }
        };
    }
}
