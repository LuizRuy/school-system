package com.school.school.model.dto.submission;


import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public class SubmissionResponse {
    private Long taskId;
    private String taskTitle;
    private LocalDateTime createdAt;
    private List<StudentSubmission> submissions;

}

