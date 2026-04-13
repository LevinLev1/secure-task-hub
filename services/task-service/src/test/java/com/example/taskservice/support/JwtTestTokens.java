package com.example.taskservice.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

public final class JwtTestTokens {

    private JwtTestTokens() {
    }

    public static String accessTokenForUser(String username, String rawSecret) {
        SecretKey secretKey = Keys.hmacShaKeyFor(rawSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", "ROLE_USER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
