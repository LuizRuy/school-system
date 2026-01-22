package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.model.dto.task.CreateTaskRequest;
import com.school.school.model.dto.task.TaskResponse;
import com.school.school.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public void createTask(CreateTaskRequest createTaskRequest, UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        Task task = new Task();
        task.setName(createTaskRequest.getName());
        task.setUser(user);
        task.setCreatedAt(LocalDateTime.now());

        taskRepository.save(task);
    }

    public TaskResponse findTaskById(Long taskId, UserAuthenticated userAuthenticated) {

        Task task = getById(taskId, userAuthenticated);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(task.getId());
        taskResponse.setName(task.getName());

        return taskResponse;
    }

    public Task getById(Long taskId, UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this task");
        }

        return task;
    }

    public List<TaskResponse> findTasksByUser(UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        List<Task> tasks = taskRepository.findByUserId(user.getId());

        return tasks.stream().map(task -> {
            TaskResponse taskResponse = new TaskResponse();
            taskResponse.setId(task.getId());
            taskResponse.setName(task.getName());
            return taskResponse;
        }).toList();
    }

    public void deleteTaskById(Long taskId, UserAuthenticated userAuthenticated) {
        Task task = getById(taskId, userAuthenticated);
        taskRepository.delete(task);
    }
}
