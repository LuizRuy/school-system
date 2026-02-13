package com.school.school.model.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CreateStudentRequest {
    @NotBlank(message = "O nome do aluno é obrigatório")
    private String name;

    @NotNull(message = "A data de nascimento do aluno é obrigatória")
    private LocalDate dateOfBirth;
}
