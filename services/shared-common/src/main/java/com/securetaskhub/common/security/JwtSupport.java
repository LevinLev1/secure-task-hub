package com.securetaskhub.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;

public final class JwtSupport {

    private JwtSupport() {
    }

    public static SecretKey secretKeyFrom(String rawSecret) {
        return Keys.hmacShaKeyFor(rawSecret.getBytes(StandardCharsets.UTF_8));
    }

    public static String createAccessToken(String username, String role, SecretKey secretKey, long expirationSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }

    public static Optional<JwtPrincipal> parseAccessToken(String token, SecretKey secretKey) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            if (username == null || role == null) {
                return Optional.empty();
            }
            return Optional.of(new JwtPrincipal(username, role));
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }
}
