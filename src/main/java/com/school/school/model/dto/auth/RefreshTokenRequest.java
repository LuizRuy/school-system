package com.school.school.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token é obrigatório")
        @Size(min = 20, max = 255, message = "Refresh token inválido")
        String refreshToken
) {
}
