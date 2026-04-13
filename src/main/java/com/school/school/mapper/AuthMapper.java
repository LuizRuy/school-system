package com.school.school.mapper;

import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.model.dto.auth.AuthResponse;
import com.school.school.model.dto.auth.RefreshTokenResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(String token, RefreshToken refreshToken, User user) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public RefreshTokenResponse toRefreshTokenResponse(String token, RefreshToken refreshToken) {
        return RefreshTokenResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
