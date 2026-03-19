package com.school.school.model.dto.submission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {
    @NotNull(message = "O id do aluno é obrigatorio")
    private Long studentId;

    @NotNull(message = "O id da tarefa é obrigatorio")
    private Long taskId;

    @NotNull(message = "Situação da tarefa é obrigatorio")
    private Boolean submitted;
}
