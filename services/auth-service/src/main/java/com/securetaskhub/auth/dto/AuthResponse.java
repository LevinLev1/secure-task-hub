package com.securetaskhub.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        String username,
        String role
) {
}
