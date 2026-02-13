package com.school.school.model.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "O nome da tarefa é obrigatorio")
    private String name;
}
