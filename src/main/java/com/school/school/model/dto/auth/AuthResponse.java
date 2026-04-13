package com.school.school.model.dto.auth;

import lombok.Builder;

@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
