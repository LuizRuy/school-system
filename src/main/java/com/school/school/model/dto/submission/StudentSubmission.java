package com.school.school.model.dto.submission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentSubmission {
    private Long studentId;
    private String studentName;
    private Boolean submitted;
}
