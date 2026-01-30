package com.school.school.model.dto.classsession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassSessionResponse {
    private Long id;
    private LocalDateTime createdAt;
}
