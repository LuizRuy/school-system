package com.school.school.model.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "O nome da tarefa é obrigatorio")
    @Size(max = 120, message = "O nome da tarefa deve ter no máximo 120 caracteres")
    private String name;
}
