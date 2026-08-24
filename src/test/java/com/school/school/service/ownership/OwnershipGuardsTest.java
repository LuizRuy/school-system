package com.school.school.service.ownership;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Classroom;
import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.repository.ClassroomRepository;
import com.school.school.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OwnershipGuardsTest {

    private final User owner = userWithId(7L);
    private final UserAuthenticated principal = new UserAuthenticated(owner);

    private static User userWithId(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private static Task task(long taskId, User user) {
        Task task = new Task();
        task.setId(taskId);
        task.setUser(user);
        return task;
    }

    private static Classroom classroom(long classroomId, User user) {
        Classroom classroom = new Classroom();
        classroom.setId(classroomId);
        classroom.setUser(user);
        return classroom;
    }

    @Test
    @DisplayName("Task guard resolves a task owned by the principal")
    void taskGuardResolvesOwnedTask() {
        TaskRepository repository = mock(TaskRepository.class);
        Task owned = task(42L, owner);
        when(repository.findById(42L)).thenReturn(Optional.of(owned));

        Task resolved = OwnershipGuards.forTasks(repository).resolve(42L, principal);

        assertThat(resolved).isSameAs(owned);
    }

    @Test
    @DisplayName("Task guard keeps the 404 clients see for a missing task")
    void taskGuardKeeps404ForMissingTask() {
        TaskRepository repository = mock(TaskRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> OwnershipGuards.forTasks(repository).resolve(99L, principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found with id: 99");
    }

    @Test
    @DisplayName("Task guard keeps the 403 clients see for a foreign task")
    void taskGuardKeeps403ForForeignTask() {
        TaskRepository repository = mock(TaskRepository.class);
        when(repository.findById(42L)).thenReturn(Optional.of(task(42L, userWithId(8L))));

        assertThatThrownBy(() -> OwnershipGuards.forTasks(repository).resolve(42L, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this task");
    }

    @Test
    @DisplayName("Classroom guard resolves a classroom owned by the principal")
    void classroomGuardResolvesOwnedClassroom() {
        ClassroomRepository repository = mock(ClassroomRepository.class);
        Classroom owned = classroom(11L, owner);
        when(repository.findById(11L)).thenReturn(Optional.of(owned));

        Classroom resolved = OwnershipGuards.forClassrooms(repository).resolve(11L, principal);

        assertThat(resolved).isSameAs(owned);
    }

    @Test
    @DisplayName("Classroom guard keeps the 404 clients see for a missing classroom")
    void classroomGuardKeeps404ForMissingClassroom() {
        ClassroomRepository repository = mock(ClassroomRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> OwnershipGuards.forClassrooms(repository).resolve(99L, principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Classroom not found with id: 99");
    }

    @Test
    @DisplayName("Classroom guard keeps the 403 clients see for a foreign classroom")
    void classroomGuardKeeps403ForForeignClassroom() {
        ClassroomRepository repository = mock(ClassroomRepository.class);
        when(repository.findById(11L)).thenReturn(Optional.of(classroom(11L, userWithId(8L))));

        assertThatThrownBy(() -> OwnershipGuards.forClassrooms(repository).resolve(11L, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this classroom");
    }

}
