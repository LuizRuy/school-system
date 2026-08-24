package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.StudentMapper;
import com.school.school.mapper.SubmissionMapper;
import com.school.school.model.Student;
import com.school.school.model.Submission;
import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.model.dto.submission.SubmissionsRequest;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubmissionRepository;
import com.school.school.repository.TaskRepository;
import com.school.school.repository.UserRepository;
import com.school.school.service.ownership.OwnershipGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({SubmissionService.class, StudentService.class, StudentMapper.class, SubmissionMapper.class,
        SubmissionIntakePersistenceTest.StubGuards.class})
class SubmissionIntakePersistenceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private OwnershipGuard<Task> taskOwnershipGuard;

    @TestConfiguration
    static class StubGuards {

        @Bean
        OwnershipGuard<Student> studentOwnershipGuard() {
            return mock(OwnershipGuard.class);
        }

        @Bean
        OwnershipGuard<Task> taskOwnershipGuard() {
            return mock(OwnershipGuard.class);
        }
    }

    @AfterEach
    void wipeTables() {
        submissionRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        taskRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private User persistedOwner() {
        return userRepository.saveAndFlush(User.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada-%s@example.com".formatted(UUID.randomUUID()))
                .password("hashed")
                .build());
    }

    private UserAuthenticated principalOf(User user) {
        return new UserAuthenticated(user);
    }

    private Task persistedTask(User owner, String name) {
        return taskRepository.saveAndFlush(Task.builder()
                .name(name)
                .user(owner)
                .build());
    }

    private Student persistedStudent(User owner, String name) {
        return studentRepository.saveAndFlush(Student.builder()
                .name(name)
                .dateOfBirth(LocalDate.of(2010, 5, 1))
                .user(owner)
                .build());
    }

    private static SubmissionsRequest batchRequest(Map<Long, Boolean> states) {
        SubmissionsRequest request = new SubmissionsRequest();
        request.setSubmissions(new LinkedHashMap<>(states));
        return request;
    }

    private void resolvedThroughGuard(Task task, UserAuthenticated principal) {
        when(taskOwnershipGuard.resolve(task.getId(), principal)).thenReturn(task);
    }

    @Test
    @DisplayName("A batch naming an unknown student is denied whole, leaving no partial rows")
    void batchWithUnknownStudentLeavesNoPartialRows() {
        User owner = persistedOwner();
        UserAuthenticated principal = principalOf(owner);
        Task algebra = persistedTask(owner, "Algebra homework");
        Student enrolled = persistedStudent(owner, "Enrolled");
        resolvedThroughGuard(algebra, principal);

        Map<Long, Boolean> states = new LinkedHashMap<>();
        states.put(enrolled.getId(), true);
        states.put(9999L, false);

        assertThatThrownBy(() -> submissionService.addSubmissions(algebra.getId(), batchRequest(states), principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student not found with id: 9999");

        assertThat(submissionRepository.count()).isZero();
    }

    @Test
    @DisplayName("The schema rejects a second submission row for the same student and task")
    void schemaRejectsDuplicatePair() {
        User owner = persistedOwner();
        Task algebra = persistedTask(owner, "Algebra homework");
        Student student = persistedStudent(owner, "Bob");

        submissionRepository.saveAndFlush(Submission.builder()
                .student(student)
                .task(algebra)
                .submitted(true)
                .build());

        assertThatThrownBy(() -> submissionRepository.saveAndFlush(Submission.builder()
                .student(student)
                .task(algebra)
                .submitted(false)
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(submissionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Re-recording persists as an update: same single row, flipped value")
    void reRecordingPersistsAsUpdateOfSameRow() {
        User owner = persistedOwner();
        UserAuthenticated principal = principalOf(owner);
        Task algebra = persistedTask(owner, "Algebra homework");
        Student student = persistedStudent(owner, "Bob");
        submissionRepository.saveAndFlush(Submission.builder()
                .student(student)
                .task(algebra)
                .submitted(true)
                .build());
        resolvedThroughGuard(algebra, principal);

        submissionService.addSubmissions(algebra.getId(), batchRequest(Map.of(student.getId(), false)), principal);

        assertThat(submissionRepository.count()).isEqualTo(1);
        Submission stored = submissionRepository.findAll().get(0);
        assertThat(stored.getStudent().getId()).isEqualTo(student.getId());
        assertThat(stored.getSubmitted()).isFalse();
    }

    @Test
    @DisplayName("Batch intake resolves the real class roster in one query and stores mixed states")
    void batchIntakeStoresMixedStatesForRealRoster() {
        User owner = persistedOwner();
        UserAuthenticated principal = principalOf(owner);
        Task algebra = persistedTask(owner, "Algebra homework");
        Student alice = persistedStudent(owner, "Alice");
        Student bob = persistedStudent(owner, "Bob");
        resolvedThroughGuard(algebra, principal);

        submissionService.addSubmissions(algebra.getId(),
                batchRequest(Map.of(alice.getId(), true, bob.getId(), false)), principal);

        assertThat(submissionRepository.count()).isEqualTo(2);
        assertThat(submissionRepository.findByTask(algebra))
                .extracting(row -> row.getStudent().getId(), Submission::getSubmitted)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(alice.getId(), true),
                        org.assertj.core.groups.Tuple.tuple(bob.getId(), false));
    }
}
