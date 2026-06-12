package com.paypal.api_gateway.filters;

import com.paypal.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public  class JwtAuthFilter implements GlobalFilter, Ordered {


    //these are paths where any user can access login and signup
    private static final List<String> PUBLIC_PATHS =List.of(
            "/api/auth/signup",
            "/api/auth/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        String path = exchange.getRequest().getPath().value();
        String normalizedPath =path.replaceAll("/+$","");

        //skipping filter for public paths
        if(PUBLIC_PATHS.contains(normalizedPath)){
            return chain.filter(exchange)
                    .doOnSubscribe(s-> System.out.println("Proceeding without check"))
                    .doOnSuccess(v-> System.out.println("Successfully passed"))
                    .doOnError(e-> System.out.println("error occured"));
        }

        //extracting token from headers authorization key
        //remember we see in postman it starts with Bearer
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        //missing token
        if(!authHeader.startsWith("Bearer ")){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try{
            //skipping first 7 char thats 'Bearer'
            String token =authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);
            exchange.getRequest().mutate()
                    .header("X-User-Email", claims.getSubject())
                    .build();
            return chain.filter(exchange)
                    .doOnSubscribe(s-> System.out.println("Proceeding without check"))
                    .doOnSuccess(v-> System.out.println("Successfully passed"))
                    .doOnError(e-> System.out.println("error occured"));
        }catch (Exception e){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
