package com.school.school.model.dto.classsession;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public class ClassSessionResponse {
    private Long id;
    private LocalDateTime createdAt;
}
