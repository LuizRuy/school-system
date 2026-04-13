package com.school.school.model.dto.user;

import java.time.LocalDateTime;

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
