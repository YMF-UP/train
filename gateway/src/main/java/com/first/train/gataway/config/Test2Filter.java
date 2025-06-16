/*
package com.first.train.gataway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class Test2Filter implements GlobalFilter, Ordered {

   public static  final Logger LOG= LoggerFactory.getLogger(Test2Filter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOG.info("Test2Filter");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
*/
