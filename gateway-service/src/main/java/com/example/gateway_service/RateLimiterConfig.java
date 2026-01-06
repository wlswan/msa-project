package com.example.gateway_service;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String username = exchange.getRequest().getHeaders().getFirst("X-User-Name");

            if (username != null && !username.isEmpty()) {
                return Mono.just("user:" + username);
            }

            return Mono.just("ip:" + Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                    .getAddress().getHostAddress());
        };
    }
}
