package br.com.fiap.inovagab.auth.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Instant expiresAt,
        UserResponse user
) {
}
