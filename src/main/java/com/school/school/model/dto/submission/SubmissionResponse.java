package com.school.school.model.dto.submission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponse {
    private Long taskId;
    private String taskTitle;
    private LocalDateTime createdAt;
    private List<StudentSubmission> submissions;

}

