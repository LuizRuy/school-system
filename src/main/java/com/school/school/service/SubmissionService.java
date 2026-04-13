package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.SubmissionMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TaskService taskService;
    private final StudentService studentService;
    private final SubmissionMapper submissionMapper;

    public void addSubmission(SubmissionRequest submissionRequest, UserAuthenticated userAuthenticated) {

        Student student = studentService.findStudent(submissionRequest.getStudentId(), userAuthenticated);
        Task task = taskService.getById(submissionRequest.getTaskId(), userAuthenticated);

        submissionRepository.save(submissionMapper.toEntity(submissionRequest, student, task));
    }

    @Transactional
    public void addSubmissions(Long taskId, SubmissionsRequest submissionsRequest, UserAuthenticated userAuthenticated) {

        Task task = taskService.getById(taskId, userAuthenticated);

        List<Submission> submissions = submissionMapper.toSubmissions(
                submissionsRequest.getSubmissions(),
                task,
                userAuthenticated
        );

        submissionRepository.saveAll(submissions);

    }

    public SubmissionResponse getSubmissions(Long taskId, UserAuthenticated userAuthenticated) {
        Task task = taskService.getById(taskId, userAuthenticated);

        List<Submission> submissions = submissionRepository.findByTask(task);

        return submissionMapper.toSubmissionResponse(task, submissions);
    }


    public void updateSubmission(SubmissionRequest submissionRequest, UserAuthenticated userAuthenticated) {

        taskService.getById(submissionRequest.getTaskId(), userAuthenticated);
        studentService.findStudent(submissionRequest.getStudentId(), userAuthenticated);

        Submission submission = submissionRepository.findByTaskIdAndStudentId(submissionRequest.getTaskId(), submissionRequest.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found for task ID " + submissionRequest.getTaskId() +
                        " and student ID " + submissionRequest.getStudentId()));

        submission.setSubmitted(submissionRequest.getSubmitted());

        submissionRepository.save(submission);

    }

}
