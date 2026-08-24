package com.school.school.infra.security;

public class ExpiredRefreshTokenException extends RuntimeException {

    public ExpiredRefreshTokenException() {
        super("Refresh token has expired. Please make a new signin request");
    }
}
