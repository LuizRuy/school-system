package com.school.school.infra.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public ApiError(int status, String message) {
        this(status, message, LocalDateTime.now(), null);
    }

    public ApiError(int status, String message, Map<String, String> fieldErrors) {
        this(status, message, LocalDateTime.now(), fieldErrors);
    }
}
