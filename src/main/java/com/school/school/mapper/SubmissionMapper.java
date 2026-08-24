package com.school.school.mapper;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.model.Student;
import com.school.school.model.Submission;
import com.school.school.model.Task;
import com.school.school.model.dto.submission.StudentSubmission;
import com.school.school.model.dto.submission.SubmissionRequest;
import com.school.school.model.dto.submission.SubmissionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SubmissionMapper {

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

    public List<Submission> toSubmissions(Map<Long, Boolean> submissionsMap, Task task, Map<Long, Student> resolvedStudents) {
        return submissionsMap.entrySet()
                .stream()
                .map(entry -> toSubmission(entry.getKey(), entry.getValue(), task, resolvedStudents))
                .toList();
    }

    public Submission toSubmission(Long studentId, Boolean submitted, Task task, Map<Long, Student> resolvedStudents) {
        Student student = resolvedStudents.get(studentId);
        if (student == null) {
            throw new EntityNotFoundException("Student not found with id: " + studentId);
        }
        return Submission.builder()
                .task(task)
                .student(student)
                .submitted(submitted)
                .build();
    }
}
