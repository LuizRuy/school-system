package com.school.school.model.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStudent {
    @NotBlank(message = "O nome do aluno é obrigatório")
    private String name;

    @NotNull(message = "A data de nascimento do aluno é obrigatória")
    private LocalDate dateOfBirth;

    @NotEmpty(message = "As observações do aluno não podem estar vazias")
    private String observations;
}
