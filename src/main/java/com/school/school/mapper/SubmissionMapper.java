package com.school.school.mapper;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Student;
import com.school.school.model.Submission;
import com.school.school.model.Task;
import com.school.school.model.dto.submission.StudentSubmission;
import com.school.school.model.dto.submission.SubmissionRequest;
import com.school.school.model.dto.submission.SubmissionResponse;
import com.school.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SubmissionMapper {

    private final StudentService studentService;

    public Submission toEntity(SubmissionRequest dto, Student student, Task task) {
        return Submission.builder()
                .student(student)
                .task(task)
                .submitted(dto.getSubmitted())
                .build();
    }

    public SubmissionResponse toSubmissionResponse(Task task, List<Submission> submissions) {
        return SubmissionResponse.builder()
                .taskId(task.getId())
                .taskTitle(task.getName())
                .createdAt(task.getCreatedAt())
                .submissions(submissions.stream()
                        .map(this::toStudentSubmission)
                        .toList())
                .build();
    }

    public StudentSubmission toStudentSubmission(Submission submission) {
        return StudentSubmission.builder()
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .submitted(submission.getSubmitted())
                .build();
    }

    public List<Submission> toSubmissions(Map<Long, Boolean> submissionsMap, Task task, UserAuthenticated userAuthenticated) {
        return submissionsMap.entrySet()
                .stream()
                .map(entry -> toSubmission(entry.getKey(), entry.getValue(), task, userAuthenticated))
                .toList();
    }

    public Submission toSubmission(Long studentId, Boolean submitted, Task task, UserAuthenticated userAuthenticated) {
        return Submission.builder()
                .task(task)
                .student(studentService.findStudent(studentId, userAuthenticated))
                .submitted(submitted)
                .build();
    }
}
