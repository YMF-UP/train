/*
package com.first.train.gataway.config;

import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;


@Component
public class TestFilter implements GlobalFilter, Ordered {

   public static  final Logger LOG= LoggerFactory.getLogger(TestFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOG.info("TestFilter");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
*/
