package com.school.school.model.dto.submission;

import lombok.Data;

@Data
public class SubmissionRequest {

    private Long studentId;
    private Long taskId;
    private Boolean submitted;
}
