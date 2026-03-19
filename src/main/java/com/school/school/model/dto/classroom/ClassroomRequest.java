package com.school.school.model.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClassroomRequest {
    @NotBlank(message = "O nome da turma é obrigatório")
    private String name;
}
