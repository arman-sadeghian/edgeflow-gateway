package com.sadeghian.edgeflow.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Correlation-Id");

        String method = exchange.getRequest()
                .getMethod()
                .name();

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        log.info(
                "request method={} path={} correlationId={}",
                method,
                path,
                correlationId
        );

        return chain.filter(exchange)
                .doFinally(signalType -> {

                    long duration =
                            System.currentTimeMillis() - startTime;

                    log.info(
                            "response method={} path={} status={} durationMs={} correlationId={}",
                            method,
                            path,
                            exchange.getResponse().getStatusCode(),
                            duration,
                            correlationId
                    );
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}