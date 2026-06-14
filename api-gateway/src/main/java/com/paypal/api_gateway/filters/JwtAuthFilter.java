package com.paypal.api_gateway.filters;

import com.paypal.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public  class JwtAuthFilter implements GlobalFilter, Ordered {


    //these are paths where any user can access login and signup
    private static final List<String> PUBLIC_PATHS =List.of(
            "/api/auth/signup",
            "/api/auth/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        System.out.println("=== FILTER ENTERED for path: " + exchange.getRequest().getPath());
        String path = exchange.getRequest().getPath().value();
        String normalizedPath =path.replaceAll("/+$","");
        System.out.println("Normalized apth:" + normalizedPath);

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
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Authorization header missing");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String token = authHeader.substring(7);
            System.out.println("🔐 JWT token extracted from header: " + token);

            // DEBUG: replace <your_expected_token_here> with actual token for debugging only
            String expectedToken = "<your_expected_token_here>";
            System.out.println("💡 Comparing tokens:");
            System.out.println("Expected token: " + expectedToken);
            System.out.println("Extracted token: " + token);

            Claims claims = JwtUtil.validateToken(token);
            System.out.println("✅ JWT validated successfully. Claims subject: " + claims.getSubject());

            // Add user email to headers for downstream services
            exchange.getRequest().mutate()
                    .header("X-User-Email", claims.getSubject())
                    .build();
            System.out.println("➡️ Added X-User-Email header to request: " + claims.getSubject());

            return chain.filter(exchange)
                    .doOnSubscribe(s -> System.out.println("➡️ Proceeding with JWT authenticated request"))
                    .doOnSuccess(v -> System.out.println("✅ Successfully passed JWT auth filter"))
                    .doOnError(e -> System.err.println("❌ Error during authenticated filter chain: " + e.getMessage()));

        } catch (Exception e) {
            System.err.println("❌ JWT validation failed: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
