package com.example.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long expirySeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expirySeconds:86400}") long expirySeconds) {
        if (secret == null || secret.length() < 32)
            throw new IllegalArgumentException("jwt.secret must be set and >= 32 chars");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirySeconds = expirySeconds;
    }

    public String generateToken(String username) {
        var now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirySeconds * 1000L))
                .signWith(key)
                .compact();
    }

    public String validateAndGetSubject(String token) {
        var body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return body.getSubject();
    }
}
