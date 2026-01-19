package com.school.school.model.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStudent {
    private String name;
    private LocalDate dateOfBirth;
    private String observations;
}
