package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.StudentMapper;
import com.school.school.model.Student;
import com.school.school.model.User;
import com.school.school.repository.StudentRepository;
import com.school.school.service.ownership.OwnershipGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentServiceTest {

    private final User owner = userWithId(7L);
    private final UserAuthenticated principal = new UserAuthenticated(owner);

    private final StudentRepository studentRepository = mock(StudentRepository.class);

    private final StudentService studentService = new StudentService(
            studentRepository, mock(StudentMapper.class), guard());

    @SuppressWarnings("unchecked")
    private <T> OwnershipGuard<T> guard() {
        return mock(OwnershipGuard.class);
    }

    private static User userWithId(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Student ownedStudent(long id, String name) {
        return Student.builder()
                .id(id)
                .name(name)
                .user(owner)
                .build();
    }

    @Test
    @DisplayName("Batch resolution loads every requested student with a single repository query")
    void batchResolutionUsesASingleQuery() {
        when(studentRepository.findAllById(Set.of(21L, 22L)))
                .thenReturn(List.of(ownedStudent(21L, "Alice"), ownedStudent(22L, "Bob")));

        Map<Long, Student> resolved = studentService.resolveAll(Set.of(21L, 22L), principal);

        assertThat(resolved)
                .containsKeys(21L, 22L)
                .extractingByKey(21L)
                .satisfies(student -> assertThat(student.getName()).isEqualTo("Alice"));
        verify(studentRepository).findAllById(Set.of(21L, 22L));
        verify(studentRepository, never()).findById(21L);
        verify(studentRepository, never()).findById(22L);
    }

    @Test
    @DisplayName("An unknown student in the batch aborts resolution as not found")
    void unknownStudentAbortsResolutionAsNotFound() {
        when(studentRepository.findAllById(Set.of(21L, 9999L)))
                .thenReturn(List.of(ownedStudent(21L, "Alice")));

        assertThatThrownBy(() -> studentService.resolveAll(Set.of(21L, 9999L), principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found with id: 9999");
    }

    @Test
    @DisplayName("A foreign student in the batch aborts resolution as access denied")
    void foreignStudentAbortsResolutionAsAccessDenied() {
        User foreignUser = userWithId(8L);
        Student foreignStudent = Student.builder()
                .id(22L)
                .name("Mallory")
                .user(foreignUser)
                .build();
        when(studentRepository.findAllById(Set.of(21L, 22L)))
                .thenReturn(List.of(ownedStudent(21L, "Alice"), foreignStudent));

        assertThatThrownBy(() -> studentService.resolveAll(Set.of(21L, 22L), principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this student");
    }

    @Test
    @DisplayName("Batch resolution never queries the repository for an empty request")
    void emptyRequestSkipsTheRepository() {
        Map<Long, Student> resolved = studentService.resolveAll(Set.of(), principal);

        assertThat(resolved).isEmpty();
        verify(studentRepository, never()).findAllById(java.util.Collections.emptySet());
    }
}
