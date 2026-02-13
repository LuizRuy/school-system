package com.school.school.model.dto.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAttendanceRequest {
    @NotNull(message = "O id da sessão de aula é obrigatório.")
    private Long classSessionId;

    @NotNull(message = "O id do aluno é obrigatório.")
    private Long studentId;

    @NotNull(message = "A presença do aluno é obrigatória.")
    private boolean presence;
}
