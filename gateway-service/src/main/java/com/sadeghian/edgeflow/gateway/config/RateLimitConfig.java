package com.sadeghian.edgeflow.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange ->
                exchange.getPrincipal()
                        .map(principal -> principal.getName())
                        .defaultIfEmpty("anonymous");
    }
}