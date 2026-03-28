package com.school.school.model.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStudent {
    @NotBlank(message = "O nome do aluno é obrigatório")
    @Size(max = 120, message = "O nome do aluno deve ter no máximo 120 caracteres")
    private String name;

    @NotNull(message = "A data de nascimento do aluno é obrigatória")
    @Past(message = "A data de nascimento deve estar no passado")
    private LocalDate dateOfBirth;

    @NotEmpty(message = "As observações do aluno não podem estar vazias")
    @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
    private String observations;
}
