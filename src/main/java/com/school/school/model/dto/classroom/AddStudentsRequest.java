package com.school.school.model.dto.classroom;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AddStudentsRequest {
    @NotEmpty
    private Set<Long> students;
}
