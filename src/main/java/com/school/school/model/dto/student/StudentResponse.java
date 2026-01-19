package com.school.school.model.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String name;
    private String observation;
    private LocalDate birthDate;
}
