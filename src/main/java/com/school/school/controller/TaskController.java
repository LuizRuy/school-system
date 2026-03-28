package com.school.school.controller;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.task.CreateTaskRequest;
import com.school.school.model.dto.task.TaskResponse;
import com.school.school.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    ResponseEntity<Void> createTask(@RequestBody @Valid CreateTaskRequest createTaskRequest,
                                    @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        taskService.createTask(createTaskRequest, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{taskId}")
    ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId,
                                             @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        TaskResponse taskResponse = taskService.findTaskById(taskId, authenticatedUser);
        return ResponseEntity.ok(taskResponse);
    }

    @GetMapping("/user-tasks")
    ResponseEntity<List<TaskResponse>> getTasksByUser(@AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        List<TaskResponse> tasks = taskService.findTasksByUser(authenticatedUser);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{taskId}")
    ResponseEntity<Void> deleteTask (@PathVariable Long taskId,
                                     @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        taskService.deleteTaskById(taskId, authenticatedUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
