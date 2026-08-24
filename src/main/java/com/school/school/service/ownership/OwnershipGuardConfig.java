package com.school.school.service.ownership;

import com.school.school.model.Classroom;
import com.school.school.model.Task;
import com.school.school.repository.ClassroomRepository;
import com.school.school.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OwnershipGuardConfig {

    @Bean
    public OwnershipGuard<Task> taskOwnershipGuard(TaskRepository taskRepository) {
        return OwnershipGuards.forTasks(taskRepository);
    }

    @Bean
    public OwnershipGuard<Classroom> classroomOwnershipGuard(ClassroomRepository classroomRepository) {
        return OwnershipGuards.forClassrooms(classroomRepository);
    }
}
