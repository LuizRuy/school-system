package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.SubmissionMapper;
import com.school.school.model.Student;
import com.school.school.model.Submission;
import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.model.dto.submission.SubmissionRequest;
import com.school.school.model.dto.submission.SubmissionsRequest;
import com.school.school.repository.SubmissionRepository;
import com.school.school.service.ownership.OwnershipGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubmissionServiceTest {

    private final User owner = userWithId(7L);
    private final UserAuthenticated principal = new UserAuthenticated(owner);

    private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
    private final OwnershipGuard<Task> taskOwnershipGuard = guard();
    private final OwnershipGuard<Student> studentOwnershipGuard = guard();
    private final StudentService studentService = mock(StudentService.class);
    private final SubmissionMapper submissionMapper = new SubmissionMapper();

    private final SubmissionService submissionService = new SubmissionService(
            submissionRepository, taskOwnershipGuard, studentOwnershipGuard, studentService, submissionMapper);

    @SuppressWarnings("unchecked")
    private <T> OwnershipGuard<T> guard() {
        return mock(OwnershipGuard.class);
    }

    private static User userWithId(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Student student(long studentId) {
        return Student.builder()
                .id(studentId)
                .name("Student " + studentId)
                .user(owner)
                .build();
    }

    private Task task(long taskId) {
        return Task.builder()
                .id(taskId)
                .name("Algebra homework")
                .user(owner)
                .build();
    }

    private void resolvedTask(long taskId) {
        when(taskOwnershipGuard.resolve(taskId, principal)).thenReturn(task(taskId));
    }

    private Task resolvedTask(Task algebra) {
        when(taskOwnershipGuard.resolve(algebra.getId(), principal)).thenReturn(algebra);
        return algebra;
    }

    private void resolvedStudents(Map<Long, Student> students) {
        when(studentService.resolveAll(students.keySet(), principal)).thenReturn(students);
    }

    private static SubmissionsRequest batchRequest(long taskId, Map<Long, Boolean> states) {
        SubmissionsRequest request = new SubmissionsRequest();
        request.setSubmissions(new LinkedHashMap<>(states));
        return request;
    }

    private static SubmissionRequest singleRequest(long studentId, long taskId, boolean submitted) {
        SubmissionRequest request = new SubmissionRequest();
        request.setStudentId(studentId);
        request.setTaskId(taskId);
        request.setSubmitted(submitted);
        return request;
    }

    private Submission storedRow(long id, long studentId, long taskId, boolean submitted) {
        return Submission.builder()
                .id(id)
                .student(student(studentId))
                .task(task(taskId))
                .submitted(submitted)
                .build();
    }

    @Test
    @DisplayName("Batch intake resolves every student through one batched lookup instead of one per row")
    void batchIntakeResolvesStudentsThroughOneBatchedLookup() {
        Task algebra = resolvedTask(task(31L));
        resolvedStudents(Map.of(21L, student(21L), 22L, student(22L)));
        when(submissionRepository.findByTask(algebra)).thenReturn(List.of());

        submissionService.addSubmissions(31L, batchRequest(31L, Map.of(21L, true, 22L, false)), principal);

        verify(studentService).resolveAll(Set.of(21L, 22L), principal);
        verify(studentService, never()).findStudent(anyLong(), any());
        verify(submissionRepository).findByTask(algebra);
        verify(submissionRepository, never()).findByTaskIdAndStudentId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Batch intake persists mixed submitted states for the whole class")
    void batchIntakePersistsMixedSubmittedStates() {
        Task algebra = resolvedTask(task(31L));
        resolvedStudents(Map.of(21L, student(21L), 22L, student(22L)));
        when(submissionRepository.findByTask(algebra)).thenReturn(List.of());

        submissionService.addSubmissions(31L, batchRequest(31L, Map.of(21L, true, 22L, false)), principal);

        ArgumentCaptor<List<Submission>> saved = ArgumentCaptor.forClass(List.class);
        verify(submissionRepository).saveAll(saved.capture());
        assertThat(saved.getValue())
                .extracting(row -> row.getStudent().getId(), row -> row.getTask().getId(), Submission::getSubmitted)
                .containsExactlyInAnyOrder(
                        tuple(21L, 31L, true),
                        tuple(22L, 31L, false));
    }

    @Test
    @DisplayName("Batch intake upserts: already-submitted students flip in place, fresh ones insert")
    void batchIntakeUpsertsAlreadySubmittedStudents() {
        Task algebra = resolvedTask(task(31L));
        resolvedStudents(Map.of(21L, student(21L), 22L, student(22L)));
        Submission alreadySubmitted = storedRow(5L, 21L, 31L, true);
        when(submissionRepository.findByTask(algebra)).thenReturn(List.of(alreadySubmitted));

        submissionService.addSubmissions(31L, batchRequest(31L, Map.of(21L, false, 22L, true)), principal);

        verify(submissionRepository).save(same(alreadySubmitted));
        assertThat(alreadySubmitted.getSubmitted()).isFalse();

        ArgumentCaptor<List<Submission>> saved = ArgumentCaptor.forClass(List.class);
        verify(submissionRepository).saveAll(saved.capture());
        assertThat(saved.getValue()).hasSize(1);
        assertThat(saved.getValue().get(0).getId()).isNull();
        assertThat(saved.getValue().get(0).getStudent().getId()).isEqualTo(22L);
        assertThat(saved.getValue().get(0).getSubmitted()).isTrue();
    }

    @Test
    @DisplayName("A foreign or unknown student denies the whole batch before any row is written")
    void deniedBatchWritesNoRows() {
        resolvedTask(31L);
        when(studentService.resolveAll(any(), any()))
                .thenThrow(new AccessDeniedException("You do not have permission to access this student"));

        assertThatThrownBy(() ->
                submissionService.addSubmissions(31L, batchRequest(31L, Map.of(21L, true)), principal))
                .isInstanceOf(AccessDeniedException.class);

        verify(submissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Single recording resolves the pair through the ownership guards and inserts a fresh row")
    void singleRecordingResolvesThroughTheGuardsAndInserts() {
        resolvedTask(31L);
        when(studentOwnershipGuard.resolve(21L, principal)).thenReturn(student(21L));
        when(submissionRepository.findByTaskIdAndStudentId(31L, 21L)).thenReturn(Optional.empty());

        submissionService.addSubmission(singleRequest(21L, 31L, true), principal);

        verify(taskOwnershipGuard).resolve(31L, principal);
        verify(studentOwnershipGuard).resolve(21L, principal);
        ArgumentCaptor<Submission> saved = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isNull();
        assertThat(saved.getValue().getStudent().getId()).isEqualTo(21L);
        assertThat(saved.getValue().getTask().getId()).isEqualTo(31L);
        assertThat(saved.getValue().getSubmitted()).isTrue();
    }

    @Test
    @DisplayName("A second single submission updates the existing row instead of duplicating it")
    void secondSingleSubmissionUpdatesExistingRow() {
        resolvedTask(31L);
        when(studentOwnershipGuard.resolve(21L, principal)).thenReturn(student(21L));
        Submission existing = storedRow(5L, 21L, 31L, true);
        when(submissionRepository.findByTaskIdAndStudentId(31L, 21L)).thenReturn(Optional.of(existing));

        submissionService.addSubmission(singleRequest(21L, 31L, false), principal);

        verify(submissionRepository).save(same(existing));
        assertThat(existing.getSubmitted()).isFalse();
    }

    @Test
    @DisplayName("The update path authorizes through projections without resolving entities")
    void updatePathAuthorizesWithoutResolvingEntities() {
        Submission existing = storedRow(5L, 21L, 31L, true);
        when(submissionRepository.findByTaskIdAndStudentId(31L, 21L)).thenReturn(Optional.of(existing));

        submissionService.updateSubmission(singleRequest(21L, 31L, false), principal);

        verify(taskOwnershipGuard).authorize(31L, principal);
        verify(studentOwnershipGuard).authorize(21L, principal);
        verify(taskOwnershipGuard, never()).resolve(any(), any());
        verify(studentOwnershipGuard, never()).resolve(any(), any());
        verify(submissionRepository).save(same(existing));
        assertThat(existing.getSubmitted()).isFalse();
    }

    @Test
    @DisplayName("The update path reports a missing submission as not found")
    void updatePathReportsMissingSubmissionAsNotFound() {
        when(submissionRepository.findByTaskIdAndStudentId(31L, 21L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                submissionService.updateSubmission(singleRequest(21L, 31L, true), principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Submission not found for task ID 31 and student ID 21");

        verify(submissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Single recording, batch recording and updating all run inside one transaction")
    void mutatingPathsAreTransactional() {
        Set<String> mutatingMethods = Set.of("addSubmission", "addSubmissions", "updateSubmission");

        for (Method method : SubmissionService.class.getDeclaredMethods()) {
            if (mutatingMethods.contains(method.getName())) {
                assertThat(method.getAnnotation(Transactional.class))
                        .as("%s must be @Transactional", method.getName())
                        .isNotNull();
            }
        }
    }
}
