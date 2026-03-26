package com.school.school.model.dto.auth;

public record RefreshTokenResponse(
        String token,
        String refreshToken
) {
}
