package com.school.school.model.dto.submission;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class StudentSubmission {
    private Long studentId;
    private String studentName;
    private Boolean submitted;
}
