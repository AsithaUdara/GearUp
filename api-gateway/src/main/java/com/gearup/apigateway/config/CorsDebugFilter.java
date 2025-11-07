package com.gearup.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Temporary debug filter to inspect CORS preflight requests hitting the gateway.
 * Logs method, path and key headers for troubleshooting OPTIONS handling.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsDebugFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(CorsDebugFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            String origin = request.getHeaders().getFirst("Origin");
            String acrm = request.getHeaders().getFirst("Access-Control-Request-Method");
            String acrh = request.getHeaders().getFirst("Access-Control-Request-Headers");
            log.info("CORS Preflight -> path={}, origin={}, reqMethod={}, reqHeaders={}",
                    request.getPath(), origin, acrm, acrh);
        }
        return chain.filter(exchange);
    }
}
