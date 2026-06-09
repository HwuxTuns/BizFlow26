package com.example.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom filter để thêm header X-Gateway-Processed vào response
 * Giúp dễ phân biệt request đi qua Gateway hay không
 */
@Component
public class GatewayResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // Thêm header vào response để chứng minh request đi qua Gateway
                    exchange.getResponse().getHeaders().add("X-Gateway-Processed", "true");
                    exchange.getResponse().getHeaders().add("X-Gateway-Timestamp", String.valueOf(System.currentTimeMillis()));
                }));
    }

    @Override
    public int getOrder() {
        // Thực thi sau các filter khác
        return Ordered.LOWEST_PRECEDENCE;
    }
}
