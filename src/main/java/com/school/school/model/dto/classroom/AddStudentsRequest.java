package com.school.school.model.dto.classroom;

import lombok.Data;

import java.util.Set;

@Data
public class AddStudentsRequest {
    private Set<Long> students;
}
