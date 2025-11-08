package com.gearup.apigateway.filter;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.gearup.apigateway.service.FirebaseAuthProvider;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseToken;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Reactive global filter for validating Firebase ID tokens.
 * <p>
 * For reactive Gateway we perform token verification on a boundedElastic scheduler
 * to avoid blocking reactor event loops. On success we add X-User-Id header
 * to be available for downstream services.
 * <p>
 * Public endpoints (like chatbot) are allowed without authentication.
 */
@Component
public class FirebaseGatewayFilter implements GlobalFilter, Ordered {

    public static final String HEADER_USER_ID = "X-User-Id";
    
    // List of public endpoints that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/chat",
        "/actuator/health",
        "/actuator/info"
    );

    private final FirebaseAuthProvider authProvider;

    public FirebaseGatewayFilter(FirebaseAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Allow public endpoints without authentication
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (auth == null || !auth.startsWith("Bearer ")) {
            // let other filters/services handle anonymous requests (or security later)
            return chain.filter(exchange);
        }

        // If Firebase is not initialized (e.g., in local dev where gateway auth is disabled),
        // skip verification here and let downstream services handle token verification.
        if (FirebaseApp.getApps().isEmpty()) {
            return chain.filter(exchange);
        }

        String token = auth.substring(7);

        // perform verification off the event-loop thread
    return Mono.fromCallable(() -> authProvider.get().verifyIdToken(token))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap((FirebaseToken decoded) -> {
                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r.headers(headers -> {
                                headers.add(HEADER_USER_ID, decoded.getUid());
                                headers.add("X-Forwarded-Uid", decoded.getUid());
                            }))
                            .build();
                    return chain.filter(mutated);
                })
                .onErrorResume(ex -> {
                    // Map verification failures to 401
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
                    byte[] bytes = "{\"error\": \"Invalid or expired token\"}".getBytes();
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                });
    }
    
    /**
     * Check if the given path is a public endpoint
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        // Ensure this runs early but after routing decisions
        return HIGHEST_PRECEDENCE + 10;
    }
}
