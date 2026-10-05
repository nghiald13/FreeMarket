package com.ldn.apigateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .filter(c -> c.getAuthentication() != null && c.getAuthentication().getPrincipal() instanceof Jwt)
                .map(c -> (Jwt) c.getAuthentication().getPrincipal())
                .flatMap(jwt -> {
//                    String accountId = jwt.getSubject();
                    String email = jwt.getClaimAsString("email");

                    // Gắn thông tin User vào Header truyền xuống Downstream Services
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
//                            .header("X-Account-Id", accountId != null ? accountId : "")
                            .header("X-Account-Email", email != null ? email : "")
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 0; // Chạy sau khi Spring Security xác thực JWT thành công
    }
}