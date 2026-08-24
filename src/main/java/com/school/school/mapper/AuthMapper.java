package com.school.school.mapper;

import com.school.school.model.User;
import com.school.school.model.dto.auth.AuthResponse;
import com.school.school.model.dto.auth.RefreshTokenResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public RefreshTokenResponse toRefreshTokenResponse(String accessToken, String refreshToken) {
        return RefreshTokenResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
