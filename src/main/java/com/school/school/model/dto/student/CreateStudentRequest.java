package com.school.school.model.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CreateStudentRequest {
    @NotBlank(message = "O nome do aluno é obrigatório")
    @Size(max = 120, message = "O nome do aluno deve ter no máximo 120 caracteres")
    private String name;

    @NotNull(message = "A data de nascimento do aluno é obrigatória")
    @Past(message = "A data de nascimento deve estar no passado")
    private LocalDate dateOfBirth;
}
