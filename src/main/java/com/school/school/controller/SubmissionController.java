package com.school.school.controller;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.submission.SubmissionRequest;
import com.school.school.model.dto.submission.SubmissionResponse;
import com.school.school.model.dto.submission.SubmissionsRequest;
import com.school.school.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/{taskId}")
    ResponseEntity<Void> addSubmissions(@PathVariable Long taskId,
                                        @RequestBody SubmissionsRequest submissionsRequest,
                                        @AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        submissionService.addSubmissions(taskId, submissionsRequest, userAuthenticated);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping
    ResponseEntity<Void> addSubmission(@RequestBody SubmissionRequest submissionRequest,
                                       @AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        submissionService.addSubmission(submissionRequest, userAuthenticated);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{taskId}")
    ResponseEntity<SubmissionResponse> getSubmissions(@PathVariable Long taskId,
                                                      @AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        SubmissionResponse submissionResponse = submissionService.getSubmissions(taskId, userAuthenticated);
        return ResponseEntity.status(HttpStatus.OK).body(submissionResponse);
    }

    @PatchMapping
    ResponseEntity<Void> updateSubmission(@RequestBody SubmissionRequest submissionRequest,
                                          @AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        submissionService.updateSubmission(submissionRequest, userAuthenticated);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
