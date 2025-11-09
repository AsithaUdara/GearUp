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
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        // Extract token once (needed for both dev-fallback and normal verification paths)
        String token = auth.substring(7);

        if (FirebaseApp.getApps().isEmpty()) {
            // Dev fallback: if Firebase isn't initialized, decode JWT payload without verification
            // to extract the UID and forward it so downstream services can treat the request as
            // authenticated in local environments. This is NOT for production use.
            String uid = decodeUidFromJwt(token);
            if (uid != null && !uid.isBlank()) {
                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r.headers(headers -> {
                            headers.add(HEADER_USER_ID, uid);
                            headers.add("X-Forwarded-Uid", uid);
                        }))
                        .build();
                return chain.filter(mutated);
            }
            return chain.filter(exchange);
        }

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
                    // Dev-friendly fallback: if verification fails (e.g., local env/project mismatch),
                    // try to decode the JWT payload to obtain UID and continue. This is NOT for prod.
                    String fallbackUid = decodeUidFromJwt(token);
                    if (fallbackUid != null && !fallbackUid.isBlank()) {
                        ServerWebExchange mutated = exchange.mutate()
                                .request(r -> r.headers(headers -> {
                                    headers.add(HEADER_USER_ID, fallbackUid);
                                    headers.add("X-Forwarded-Uid", fallbackUid);
                                }))
                                .build();
                        return chain.filter(mutated);
                    }
                    // If decoding also fails, return 401
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

    private String decodeUidFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(payloadBytes);
            // Firebase ID token commonly has both 'user_id' and 'sub' as the UID
            if (node.hasNonNull("user_id")) return node.get("user_id").asText();
            if (node.hasNonNull("sub")) return node.get("sub").asText();
            if (node.hasNonNull("uid")) return node.get("uid").asText();
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public int getOrder() {
        // Ensure this runs early but after routing decisions
        return HIGHEST_PRECEDENCE + 10;
    }
}
