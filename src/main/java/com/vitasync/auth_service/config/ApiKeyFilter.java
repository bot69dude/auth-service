package com.vitasync.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Simple API Key filter to protect inter-service endpoints.
 * Looks for X-API-Key header and validates against configured key.
 */
@Component
public class ApiKeyFilter implements WebFilter {

    @Value("${internal.api.key:}")
    private String apiKey;

    // endpoints that require API key (inter-service)
    private static final List<String> PROTECTED_PATHS = List.of(
            "/auth/user/" // e.g., GET /auth/user/{id}
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        boolean requiresKey = PROTECTED_PATHS.stream().anyMatch(path::startsWith);
        if (!requiresKey) {
            return chain.filter(exchange);
        }

        String headerKey = request.getHeaders().getFirst("X-API-Key");
        if (apiKey == null || apiKey.isBlank() || headerKey == null || !apiKey.equals(headerKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
