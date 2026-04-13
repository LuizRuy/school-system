package com.school.school.model.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
