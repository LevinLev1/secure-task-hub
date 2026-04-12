package com.example.authservice.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        String username,
        String role
) {
}
