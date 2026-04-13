package com.school.school.model.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String status
) {
}
