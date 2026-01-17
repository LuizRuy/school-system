package com.school.school.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AuthResponse {
    private String token;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
