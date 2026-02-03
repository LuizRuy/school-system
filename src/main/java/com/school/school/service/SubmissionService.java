package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Student;
import com.school.school.model.Submission;
import com.school.school.model.Task;
import com.school.school.model.dto.submission.StudentSubmission;
import com.school.school.model.dto.submission.SubmissionRequest;
import com.school.school.model.dto.submission.SubmissionResponse;
import com.school.school.model.dto.submission.SubmissionsRequest;
import com.school.school.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TaskService taskService;
    private final StudentService studentService;

    public void addSubmission(SubmissionRequest submissionRequest, UserAuthenticated userAuthenticated) {

        Student student = studentService.findStudent(submissionRequest.getStudentId(), userAuthenticated);
        Task task = taskService.getById(submissionRequest.getTaskId(), userAuthenticated);

        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setTask(task);
        submission.setSubmitted(submissionRequest.getSubmitted());

        submissionRepository.save(submission);
    }

    public void addSubmissions(Long taskId, SubmissionsRequest submissionsRequest, UserAuthenticated userAuthenticated) {

        Task task = taskService.getById(taskId, userAuthenticated);

        List<Submission> submissions = submissionsRequest.getSubmissions().entrySet()
                .stream()
                .map(entry -> {
                    Submission s = new Submission();
                    s.setTask(task);
                    s.setStudent(
                            studentService.getReferenceById(entry.getKey())
                    );
                    s.setSubmitted(entry.getValue());
                    return s;
                })
                .toList();

        submissionRepository.saveAll(submissions);

    }

    public SubmissionResponse getSubmissions(Long taskId, UserAuthenticated userAuthenticated) {
        Task task = taskService.getById(taskId, userAuthenticated);

        List<Submission> submissions = submissionRepository.findByTask(task);

        return new SubmissionResponse(
                task.getId(),
                task.getName(),
                task.getCreatedAt(),
                submissions.stream().map(submission -> {
                    StudentSubmission studentSubmission = new StudentSubmission();
                    studentSubmission.setStudentId(submission.getStudent().getId());
                    studentSubmission.setStudentName(submission.getStudent().getName());
                    studentSubmission.setSubmitted(submission.getSubmitted());
                    return studentSubmission;
                }).toList()
        );
    }


    public void updateSubmission(SubmissionRequest submissionRequest, UserAuthenticated userAuthenticated) {

        Submission submission = submissionRepository.findByTaskIdAndStudentId(submissionRequest.getTaskId(), submissionRequest.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found for task ID " + submissionRequest.getTaskId() +
                        " and student ID " + submissionRequest.getStudentId()));

        submission.setSubmitted(submissionRequest.getSubmitted());

        submissionRepository.save(submission);

    }

}
