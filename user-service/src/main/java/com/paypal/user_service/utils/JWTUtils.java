package com.paypal.user_service.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtils {

    private static final String SECRET = "secret123secret123secret123secret123";

    //to get the signed key
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    private io.jsonwebtoken.Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();

    }

    public boolean validateToken(String token, String username) {
        try {
            String extractedEmail = extractEmail(token);
            return extractedEmail != null && !extractedEmail.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String generateToken(Map<String, Object> claims, String email) {

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey())
                .compact();

    }

    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }
}
