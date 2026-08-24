package com.school.school.service.ownership;

import com.school.school.model.Classroom;
import com.school.school.model.Task;
import com.school.school.repository.ClassroomRepository;
import com.school.school.repository.TaskRepository;

import java.util.Optional;
import java.util.function.Function;

public final class OwnershipGuards {

    private OwnershipGuards() {
    }

    public static <T> OwnershipGuard<T> ownedBy(
            Function<Long, Optional<T>> finder,
            Function<T, Long> ownerIdOf,
            String entityType,
            OwnershipErrorMode errorMode
    ) {
        return new RepositoryOwnershipGuard<>(finder, ownerIdOf, entityType, errorMode);
    }

    public static OwnershipGuard<Task> forTasks(TaskRepository taskRepository) {
        return ownedBy(
                taskRepository::findById,
                task -> task.getUser().getId(),
                "Task",
                OwnershipErrorMode.FORBIDDEN
        );
    }

    public static OwnershipGuard<Classroom> forClassrooms(ClassroomRepository classroomRepository) {
        return ownedBy(
                classroomRepository::findById,
                classroom -> classroom.getUser().getId(),
                "Classroom",
                OwnershipErrorMode.FORBIDDEN
        );
    }
}
