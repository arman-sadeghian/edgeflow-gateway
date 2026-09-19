package com.sadeghian.edgeflow.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/prometheus"
                        )
                        .permitAll()

                        .pathMatchers("/api/users/**")
                        .hasAnyRole("USER", "ADMIN")

                        .pathMatchers("/api/payments", "/api/payments/**")
                        .hasAnyRole("PAYMENT", "ADMIN")

                        .anyExchange()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        this::convertJwt
                                )
                        )
                )

                .build();
    }

    private Mono<AbstractAuthenticationToken> convertJwt(Jwt jwt) {

        Collection<SimpleGrantedAuthority> authorities =
                new ArrayList<>();

        Map<String, Object> resourceAccess =
                jwt.getClaimAsMap("resource_access");

        if (resourceAccess != null) {

            Object clientObject =
                    resourceAccess.get("edgeflow-gateway");

            if (clientObject instanceof Map<?, ?> clientAccess) {

                Object rolesObject =
                        clientAccess.get("roles");

                if (rolesObject instanceof List<?> roles) {

                    roles.stream()
                            .map(Object::toString)
                            .map(role -> "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                }
            }
        }

        return Mono.just(
                new JwtAuthenticationToken(jwt, authorities)
        );
    }
}