package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Attendance;
import com.school.school.model.ClassSession;
import com.school.school.model.Student;
import com.school.school.model.User;
import com.school.school.model.dto.attendance.AttendanceRequest;
import com.school.school.model.dto.attendance.UpdateAttendanceRequest;
import com.school.school.repository.AttendanceRepository;
import com.school.school.service.ownership.OwnershipGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttendanceServiceTest {

    private final User owner = userWithId(7L);
    private final UserAuthenticated principal = new UserAuthenticated(owner);

    private final AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
    private final OwnershipGuard<Student> studentOwnershipGuard = guard();
    private final OwnershipGuard<ClassSession> classSessionOwnershipGuard = guard();

    private final AttendanceService attendanceService = new AttendanceService(
            attendanceRepository, studentOwnershipGuard, classSessionOwnershipGuard);

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
        Student student = new Student();
        student.setId(studentId);
        student.setUser(owner);
        return student;
    }

    private ClassSession classSession(long classSessionId) {
        ClassSession classSession = new ClassSession();
        classSession.setId(classSessionId);
        classSession.setUser(owner);
        return classSession;
    }

    private void resolved(long studentId, long classSessionId) {
        when(classSessionOwnershipGuard.resolve(classSessionId, principal)).thenReturn(classSession(classSessionId));
        when(studentOwnershipGuard.resolve(studentId, principal)).thenReturn(student(studentId));
    }

    private static AttendanceRequest listRequest(long classSessionId, Map<Long, Boolean> studentsPresence) {
        AttendanceRequest request = new AttendanceRequest();
        request.setClassSessionId(classSessionId);
        request.setStudentsPresence(studentsPresence);
        return request;
    }

    private static UpdateAttendanceRequest updateRequest(long studentId, long classSessionId, boolean presence) {
        UpdateAttendanceRequest request = new UpdateAttendanceRequest();
        request.setStudentId(studentId);
        request.setClassSessionId(classSessionId);
        request.setPresence(presence);
        return request;
    }

    private static Attendance markedRow(long id, Student student, ClassSession classSession, boolean present) {
        return Attendance.builder()
                .id(id)
                .student(student)
                .classSession(classSession)
                .present(present)
                .build();
    }

    @Test
    @DisplayName("Recording resolves the session and student through the ownership guards")
    void recordingResolvesThroughTheGuards() {
        resolved(21L, 31L);
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.empty());

        attendanceService.markAttendance(21L, 31L, true, principal);

        verify(classSessionOwnershipGuard).resolve(31L, principal);
        verify(studentOwnershipGuard).resolve(21L, principal);
    }

    @Test
    @DisplayName("A fresh mark inserts one attendance row for the resolved pair")
    void freshMarkInsertsOneRow() {
        resolved(21L, 31L);
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.empty());

        attendanceService.markAttendance(21L, 31L, true, principal);

        ArgumentCaptor<Attendance> saved = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isNull();
        assertThat(saved.getValue().getStudent().getId()).isEqualTo(21L);
        assertThat(saved.getValue().getClassSession().getId()).isEqualTo(31L);
        assertThat(saved.getValue().isPresent()).isTrue();
    }

    @Test
    @DisplayName("Re-marking an already-marked pair flips the existing row instead of inserting a duplicate")
    void reMarkUpdatesExistingRowInPlace() {
        resolved(21L, 31L);
        Attendance existing = markedRow(5L, student(21L), classSession(31L), true);
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.of(existing));

        attendanceService.markAttendance(21L, 31L, false, principal);

        verify(attendanceRepository).save(same(existing));
        assertThat(existing.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Whole-session recording inserts one row per freshly marked student")
    void wholeSessionRecordingInsertsRowsPerFreshlyMarkedStudent() {
        resolved(21L, 31L);
        resolved(22L, 31L);
        when(attendanceRepository.findByStudentIdAndClassSessionId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        attendanceService.markAttendanceList(listRequest(31L, Map.of(21L, true, 22L, false)), principal);

        ArgumentCaptor<Attendance> saved = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues())
                .extracting(row -> row.getStudent().getId(), Attendance::isPresent)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(21L, true),
                        org.assertj.core.groups.Tuple.tuple(22L, false));
    }

    @Test
    @DisplayName("Whole-session recording upserts: already-marked students update in place, unmarked ones insert")
    void wholeSessionRecordingUpserts() {
        resolved(21L, 31L);
        resolved(22L, 31L);
        Attendance alreadyMarked = markedRow(5L, student(21L), classSession(31L), true);
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L))
                .thenReturn(Optional.of(alreadyMarked));
        when(attendanceRepository.findByStudentIdAndClassSessionId(22L, 31L))
                .thenReturn(Optional.empty());

        attendanceService.markAttendanceList(listRequest(31L, Map.of(21L, false, 22L, true)), principal);

        ArgumentCaptor<Attendance> saved = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).anySatisfy(row -> {
            assertThat(row).isSameAs(alreadyMarked);
            assertThat(row.isPresent()).isFalse();
        });
        assertThat(saved.getAllValues()).anySatisfy(row -> {
            assertThat(row.getId()).isNull();
            assertThat(row.getStudent().getId()).isEqualTo(22L);
            assertThat(row.isPresent()).isTrue();
        });
    }

    @Test
    @DisplayName("The update path authorizes through the guards without resolving entities")
    void updatePathAuthorizesThroughTheGuardsWithoutResolvingEntities() {
        Attendance existing = markedRow(5L, student(21L), classSession(31L), true);
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.of(existing));

        attendanceService.updateAttendanceList(updateRequest(21L, 31L, false), principal);

        verify(classSessionOwnershipGuard).authorize(31L, principal);
        verify(studentOwnershipGuard).authorize(21L, principal);
        verify(classSessionOwnershipGuard, never()).resolve(any(), any());
        verify(studentOwnershipGuard, never()).resolve(any(), any());
        verify(attendanceRepository).save(same(existing));
        assertThat(existing.isPresent()).isFalse();
    }

    @Test
    @DisplayName("The update path reports a missing attendance row as not found")
    void updatePathReportsMissingRowAsNotFound() {
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                attendanceService.updateAttendanceList(updateRequest(21L, 31L, true), principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Attendance record not found for student ID 21 and class session ID 31");

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Recording against a foreign student is denied before any row is written")
    void foreignStudentDeniesRecordingBeforeWriting() {
        when(classSessionOwnershipGuard.resolve(31L, principal)).thenReturn(classSession(31L));
        when(studentOwnershipGuard.resolve(21L, principal))
                .thenThrow(new AccessDeniedException("You do not have permission to access this student"));

        assertThatThrownBy(() -> attendanceService.markAttendance(21L, 31L, true, principal))
                .isInstanceOf(AccessDeniedException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Recording against a foreign session is masked as not found by the guard")
    void foreignSessionIsMaskedAsNotFound() {
        when(classSessionOwnershipGuard.resolve(31L, principal))
                .thenThrow(new EntityNotFoundException("Class session not found with id: 31"));

        assertThatThrownBy(() -> attendanceService.markAttendance(21L, 31L, true, principal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Class session not found with id: 31");

        verify(studentOwnershipGuard, never()).resolve(anyLong(), any());
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Single-student and whole-session recording plus updates all run inside one transaction")
    void mutatingPathsAreTransactional() {
        Set<String> mutatingMethods = Set.of("markAttendance", "markAttendanceList", "updateAttendanceList");

        for (Method method : AttendanceService.class.getDeclaredMethods()) {
            if (mutatingMethods.contains(method.getName())) {
                assertThat(method.getAnnotation(Transactional.class))
                        .as("%s must be @Transactional", method.getName())
                        .isNotNull();
            }
        }
    }

}
