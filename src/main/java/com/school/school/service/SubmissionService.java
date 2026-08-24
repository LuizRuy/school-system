package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.SubmissionMapper;
import com.school.school.model.Student;
import com.school.school.model.Submission;
import com.school.school.model.Task;
import com.school.school.model.dto.submission.SubmissionRequest;
import com.school.school.model.dto.submission.SubmissionResponse;
import com.school.school.model.dto.submission.SubmissionsRequest;
import com.school.school.repository.SubmissionRepository;
import com.school.school.service.ownership.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final OwnershipGuard<Task> taskOwnershipGuard;
    private final OwnershipGuard<Student> studentOwnershipGuard;
    private final StudentService studentService;
    private final SubmissionMapper submissionMapper;

    @Transactional
    public void addSubmission(SubmissionRequest submissionRequest, UserAuthenticated userAuthenticated) {

        Task task = taskOwnershipGuard.resolve(submissionRequest.getTaskId(), userAuthenticated);
        Student student = studentOwnershipGuard.resolve(submissionRequest.getStudentId(), userAuthenticated);

        record(submissionMapper.toEntity(submissionRequest, student, task));
    }

    @Transactional
    public void addSubmissions(Long taskId, SubmissionsRequest submissionsRequest, UserAuthenticated userAuthenticated) {

        Task task = taskOwnershipGuard.resolve(taskId, userAuthenticated);

        List<Submission> submissions = submissionMapper.toSubmissions(
                submissionsRequest.getSubmissions(),
                task,
                studentService.resolveAll(submissionsRequest.getSubmissions().keySet(), userAuthenticated)
        );

        submissions.forEach(this::record);
    }

    public SubmissionResponse getSubmissions(Long taskId, UserAuthenticated userAuthenticated) {
        Task task = taskOwnershipGuard.resolve(taskId, userAuthenticated);

        List<Submission> submissions = submissionRepository.findByTask(task);

        return submissionMapper.toSubmissionResponse(task, submissions);
    }


    @Transactional
    public void updateSubmission(SubmissionRequest submissionRequest, UserAuthenticated userAuthenticated) {

        taskOwnershipGuard.authorize(submissionRequest.getTaskId(), userAuthenticated);
        studentOwnershipGuard.authorize(submissionRequest.getStudentId(), userAuthenticated);

        Submission submission = submissionRepository.findByTaskIdAndStudentId(submissionRequest.getTaskId(), submissionRequest.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found for task ID " + submissionRequest.getTaskId() +
                        " and student ID " + submissionRequest.getStudentId()));

        submission.setSubmitted(submissionRequest.getSubmitted());

        submissionRepository.save(submission);

    }

    private void record(Submission incoming) {
        submissionRepository.findByTaskIdAndStudentId(incoming.getTask().getId(), incoming.getStudent().getId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setSubmitted(incoming.getSubmitted());
                            submissionRepository.save(existing);
                        },
                        () -> submissionRepository.save(incoming));
    }

}
