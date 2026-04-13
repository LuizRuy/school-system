package com.school.school.mapper;

import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.model.dto.task.CreateTaskRequest;
import com.school.school.model.dto.task.TaskResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskMapper {

    public Task toEntity(CreateTaskRequest dto, User user) {
       return Task.builder()
               .name(dto.getName())
               .user(user)
               .createdAt(LocalDateTime.now())
               .build();

    }

    public TaskResponse toDTO(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .build();
    }
}
