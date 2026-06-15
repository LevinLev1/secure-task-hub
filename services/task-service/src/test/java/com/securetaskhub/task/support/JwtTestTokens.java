package com.securetaskhub.task.support;

import com.securetaskhub.common.security.JwtSupport;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

public final class JwtTestTokens {

    private JwtTestTokens() {
    }

    public static String accessTokenForUser(String username, String rawSecret) {
        return accessTokenForRole(username, "ROLE_USER", rawSecret);
    }

    public static String accessTokenForRole(String username, String role, String rawSecret) {
        SecretKey secretKey = JwtSupport.secretKeyFrom(rawSecret);
        return JwtSupport.createAccessToken(username, role, secretKey, 3600);
    }

    public static String expiredTokenForUser(String username, String rawSecret) {
        SecretKey secretKey = JwtSupport.secretKeyFrom(rawSecret);
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", "ROLE_USER")
                .issuedAt(Date.from(now.minusSeconds(7200)))
                .expiration(Date.from(now.minusSeconds(3600)))
                .signWith(secretKey)
                .compact();
    }
}
