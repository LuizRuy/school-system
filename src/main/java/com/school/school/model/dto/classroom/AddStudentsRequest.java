package com.school.school.model.dto.classroom;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Set;

@Data
public class AddStudentsRequest {
    @NotEmpty
    private Set<@NotNull(message = "Id do aluno é obrigatório") @Positive(message = "Id do aluno deve ser maior que zero") Long> students;
}
