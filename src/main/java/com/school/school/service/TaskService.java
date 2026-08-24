package com.school.school.service;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.TaskMapper;
import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.model.dto.task.CreateTaskRequest;
import com.school.school.model.dto.task.TaskResponse;
import com.school.school.repository.TaskRepository;
import com.school.school.service.ownership.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final OwnershipGuard<Task> taskOwnershipGuard;

    public void createTask(CreateTaskRequest createTaskRequest, UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        taskRepository.save(taskMapper.toEntity(createTaskRequest, user));
    }

    public TaskResponse findTaskById(Long taskId, UserAuthenticated userAuthenticated) {

        Task task = getById(taskId, userAuthenticated);

        return taskMapper.toDTO(task);
    }

    public Task getById(Long taskId, UserAuthenticated userAuthenticated) {
        return taskOwnershipGuard.resolve(taskId, userAuthenticated);
    }

    public List<TaskResponse> findTasksByUser(UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        List<Task> tasks = taskRepository.findByUserId(user.getId());

        return tasks.stream()
                .map(taskMapper::toDTO)
                .toList();
    }

    public void deleteTaskById(Long taskId, UserAuthenticated userAuthenticated) {
        Task task = getById(taskId, userAuthenticated);
        taskRepository.delete(task);
    }
}
