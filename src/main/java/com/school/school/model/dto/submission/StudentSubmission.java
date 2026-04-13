package com.school.school.model.dto.submission;

import lombok.Builder;


@Builder
public class StudentSubmission {
    private Long studentId;
    private String studentName;
    private Boolean submitted;
}
