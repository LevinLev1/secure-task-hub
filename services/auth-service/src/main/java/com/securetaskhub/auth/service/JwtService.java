package com.securetaskhub.auth.service;

import com.securetaskhub.auth.model.Role;
import com.securetaskhub.common.security.JwtSupport;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.secretKey = JwtSupport.secretKeyFrom(secret);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String username, Role role) {
        return JwtSupport.createAccessToken(username, role.name(), secretKey, expirationSeconds);
    }
}
