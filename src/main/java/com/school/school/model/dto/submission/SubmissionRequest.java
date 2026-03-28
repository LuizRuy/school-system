package com.school.school.model.dto.submission;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SubmissionRequest {
    @NotNull(message = "O id do aluno é obrigatorio")
    @Positive(message = "O id do aluno deve ser maior que zero")
    private Long studentId;

    @NotNull(message = "O id da tarefa é obrigatorio")
    @Positive(message = "O id da tarefa deve ser maior que zero")
    private Long taskId;

    @NotNull(message = "Situação da tarefa é obrigatorio")
    private Boolean submitted;
}
