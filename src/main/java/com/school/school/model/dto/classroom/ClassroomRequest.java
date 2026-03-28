package com.school.school.model.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassroomRequest {
    @NotBlank(message = "O nome da turma é obrigatório")
    @Size(max = 120, message = "O nome da turma deve ter no máximo 120 caracteres")
    private String name;
}
