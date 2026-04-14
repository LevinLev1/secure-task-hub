package com.example.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.authservice.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-signing-12345";

    @Test
    void generateTokenContainsExpectedSubjectAndRole() {
        JwtService jwtService = new JwtService(SECRET, 3600);

        String token = jwtService.generateToken("alice", Role.ROLE_USER);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("alice", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role", String.class));
    }

    @Test
    void generateTokenSetsIssuedAtAndExpirationWindow() {
        JwtService jwtService = new JwtService(SECRET, 900);

        String token = jwtService.generateToken("alice", Role.ROLE_ADMIN);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        long windowSeconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertTrue(windowSeconds >= 899 && windowSeconds <= 901);
    }
}
