package com.school.school.model.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CreateStudentRequest {
    private String name;
    private LocalDate dateOfBirth;
}
