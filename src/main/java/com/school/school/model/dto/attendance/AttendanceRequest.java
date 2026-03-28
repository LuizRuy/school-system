package com.school.school.model.dto.attendance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class AttendanceRequest {
    @NotNull(message = "O id da sessão de aula é obrigatório.")
    @Positive(message = "O id da sessão de aula deve ser maior que zero.")
    private Long classSessionId;

    @NotEmpty(message = "A lista de presença não pode estar vazia")
    private Map<@NotNull(message = "O id do aluno é obrigatório") @Positive(message = "O id do aluno deve ser maior que zero") Long,
            @NotNull(message = "A presença do aluno é obrigatória") Boolean> studentsPresence;

}
