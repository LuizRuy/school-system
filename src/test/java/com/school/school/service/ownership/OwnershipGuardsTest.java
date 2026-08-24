package com.school.school.service.ownership;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.ClassSession;
import com.school.school.model.Classroom;
import com.school.school.model.Student;
import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.repository.ClassSessionRepository;
import com.school.school.repository.ClassroomRepository;
import com.school.school.repository.StudentRepository;
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

    private static Student student(long studentId, User user) {
        Student student = new Student();
        student.setId(studentId);
        student.setUser(user);
        return student;
    }

    private static ClassSession classSession(long classSessionId, User user) {
        ClassSession classSession = new ClassSession();
        classSession.setId(classSessionId);
        classSession.setUser(user);
        return classSession;
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

    @Test
    @DisplayName("Student guard resolves a student owned by the principal")
    void studentGuardResolvesOwnedStudent() {
        StudentRepository repository = mock(StudentRepository.class);
        Student owned = student(21L, owner);
        when(repository.findById(21L)).thenReturn(Optional.of(owned));

        Student resolved = OwnershipGuards.forStudents(repository).resolve(21L, principal);

        assertThat(resolved).isSameAs(owned);
    }

    @Test
    @DisplayName("Student guard keeps the 404 clients see for a missing student")
    void studentGuardKeeps404ForMissingStudent() {
        StudentRepository repository = mock(StudentRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> OwnershipGuards.forStudents(repository).resolve(99L, principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found with id: 99");
    }

    @Test
    @DisplayName("Student guard keeps the 403 clients see for a foreign student")
    void studentGuardKeeps403ForForeignStudent() {
        StudentRepository repository = mock(StudentRepository.class);
        when(repository.findById(21L)).thenReturn(Optional.of(student(21L, userWithId(8L))));

        assertThatThrownBy(() -> OwnershipGuards.forStudents(repository).resolve(21L, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this student");
    }

    @Test
    @DisplayName("Class session guard resolves a class session owned by the principal")
    void classSessionGuardResolvesOwnedClassSession() {
        ClassSessionRepository repository = mock(ClassSessionRepository.class);
        ClassSession owned = classSession(31L, owner);
        when(repository.findById(31L)).thenReturn(Optional.of(owned));

        ClassSession resolved = OwnershipGuards.forClassSessions(repository).resolve(31L, principal);

        assertThat(resolved).isSameAs(owned);
    }

    @Test
    @DisplayName("Class session guard keeps the 404 clients see for a missing class session")
    void classSessionGuardKeeps404ForMissingClassSession() {
        ClassSessionRepository repository = mock(ClassSessionRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> OwnershipGuards.forClassSessions(repository).resolve(99L, principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Class session not found with id: 99");
    }

    @Test
    @DisplayName("Class session guard deliberately masks a foreign class session as 404")
    void classSessionGuardMasksForeignClassSessionAsNotFound() {
        ClassSessionRepository repository = mock(ClassSessionRepository.class);
        when(repository.findById(31L)).thenReturn(Optional.of(classSession(31L, userWithId(8L))));

        assertThatThrownBy(() -> OwnershipGuards.forClassSessions(repository).resolve(31L, principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Class session not found with id: 31");
    }

}
