package com.school.school.controller;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.classsession.ClassSessionResponse;
import com.school.school.service.ClassSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/class-sessions")
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    @PostMapping
    public ResponseEntity<Void> create(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        classSessionService.createClassSession(userAuthenticated);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{classSessionId}")
    public ResponseEntity<ClassSessionResponse> getClassSessionById(@PathVariable Long classSessionId,
                                                                    @AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        ClassSessionResponse classSessionResponse = classSessionService.getById(classSessionId, userAuthenticated);
        return ResponseEntity.status(HttpStatus.OK).body(classSessionResponse);
    }

    @GetMapping("/user-classSessions")
    public ResponseEntity<List<ClassSessionResponse>> getClassSessionsByUserId(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        List<ClassSessionResponse> classSessions = classSessionService.getUserClassSessions(userAuthenticated);
        return ResponseEntity.status(HttpStatus.OK).body(classSessions);
    }

    @DeleteMapping("/{classSessionId}")
    public ResponseEntity<Void> deleteClassSessionById(@PathVariable Long classSessionId, @AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        classSessionService.deleteClassSession(classSessionId, userAuthenticated);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
