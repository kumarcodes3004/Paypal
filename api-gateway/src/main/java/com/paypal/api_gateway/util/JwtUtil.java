package com.paypal.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;

public class JwtUtil {

    private static final String SECRET="secret123secret123secret123secret123";



    //converts our secret key to proper cryptographic key object
    //this key is used to sign and validate token
    private static SecretKey getSigningKey(){

        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }



    public static Claims validateToken(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getBody();

        //basically token is like inside a envelope locked with a key
        //that contains details like user id etc and signing key is required to open that
        //so if signing key is wrong it will give error
    }

}
