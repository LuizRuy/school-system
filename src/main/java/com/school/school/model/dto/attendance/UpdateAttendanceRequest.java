package com.school.school.model.dto.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateAttendanceRequest {
    @NotNull(message = "O id da sessão de aula é obrigatório.")
    @Positive(message = "O id da sessão de aula deve ser maior que zero.")
    private Long classSessionId;

    @NotNull(message = "O id do aluno é obrigatório.")
    @Positive(message = "O id do aluno deve ser maior que zero.")
    private Long studentId;

    @NotNull(message = "A presença do aluno é obrigatória.")
    private Boolean presence;
}
