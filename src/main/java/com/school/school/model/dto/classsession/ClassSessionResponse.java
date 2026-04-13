package com.school.school.model.dto.classsession;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ClassSessionResponse {
    private Long id;
    private LocalDateTime createdAt;
}
