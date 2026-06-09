package com.example.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    @Bean
    public GlobalFilter corsFilter() {
        return new GlobalFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                String origin = exchange.getRequest().getHeaders().getOrigin();
                boolean isAllowed = origin != null && isAllowedOrigin(origin);
                
                if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                    if (isAllowed) {
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Origin", origin);
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Headers", "*");
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Credentials", "true");
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.OK);
                    return exchange.getResponse().setComplete();
                }
                
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    if (isAllowed) {
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Origin", origin);
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Headers", "*");
                        exchange.getResponse().getHeaders().set("Access-Control-Allow-Credentials", "true");
                        exchange.getResponse().getHeaders().set("Access-Control-Expose-Headers", "Authorization, Content-Type");
                    }
                }));
            }
        };
    }

    private boolean isAllowedOrigin(String origin) {
        return origin.equals("http://localhost:3000") || 
               origin.equals("http://localhost:8000") || 
               origin.equals("http://localhost:8080");
    }
}
