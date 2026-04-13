package com.school.school.model.dto.auth;

import lombok.Builder;

@Builder
public record RefreshTokenResponse(
        String token,
        String refreshToken
) {
}
