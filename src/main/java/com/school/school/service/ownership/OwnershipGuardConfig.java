package com.school.school.service.ownership;

import com.school.school.model.ClassSession;
import com.school.school.model.Classroom;
import com.school.school.model.Student;
import com.school.school.model.Task;
import com.school.school.repository.ClassSessionRepository;
import com.school.school.repository.ClassroomRepository;
import com.school.school.repository.StudentRepository;
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

    @Bean
    public OwnershipGuard<Student> studentOwnershipGuard(StudentRepository studentRepository) {
        return OwnershipGuards.forStudents(studentRepository);
    }

    @Bean
    public OwnershipGuard<ClassSession> classSessionOwnershipGuard(ClassSessionRepository classSessionRepository) {
        return OwnershipGuards.forClassSessions(classSessionRepository);
    }
}
