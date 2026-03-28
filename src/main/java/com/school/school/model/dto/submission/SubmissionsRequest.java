package com.school.school.model.dto.submission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class SubmissionsRequest {

    @NotEmpty(message = "A lista de submissões não pode estar vazia")
    private Map<@NotNull(message = "Id do aluno é obrigatório") @Positive(message = "Id do aluno deve ser maior que zero") Long,
            @NotNull(message = "Situação da submissão é obrigatória") Boolean> submissions;

}
