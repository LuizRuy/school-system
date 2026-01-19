package com.school.school.model.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {

    private String email;
    private String password;
}
