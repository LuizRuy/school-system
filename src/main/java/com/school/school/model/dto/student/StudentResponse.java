package com.school.school.model.dto.student;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public class StudentResponse {
    private Long id;
    private String name;
    private String observation;
    private LocalDate birthDate;
}
