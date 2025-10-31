package com.gearup.apigateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.gearup.apigateway.service.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseToken;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Reactive global filter for validating Firebase ID tokens.
 * <p>
 * For reactive Gateway we perform token verification on a boundedElastic scheduler
 * to avoid blocking reactor event loops. On success we add X-User-Id header
 * to be available for downstream services.
 */
@Component
public class FirebaseGatewayFilter implements GlobalFilter, Ordered {

    public static final String HEADER_USER_ID = "X-User-Id";

    private final FirebaseAuthProvider authProvider;

    public FirebaseGatewayFilter(FirebaseAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (auth == null || !auth.startsWith("Bearer ")) {
            // let other filters/services handle anonymous requests (or security later)
            return chain.filter(exchange);
        }

        String token = auth.substring(7);

        // perform verification off the event-loop thread
    return Mono.fromCallable(() -> authProvider.get().verifyIdToken(token))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap((FirebaseToken decoded) -> {
                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r.headers(headers -> headers.add(HEADER_USER_ID, decoded.getUid())))
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

    @Override
    public int getOrder() {
        // Ensure this runs early but after routing decisions
        return HIGHEST_PRECEDENCE + 10;
    }
}
