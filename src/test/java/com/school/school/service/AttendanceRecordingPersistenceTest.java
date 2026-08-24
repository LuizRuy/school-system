package com.school.school.service;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Attendance;
import com.school.school.model.ClassSession;
import com.school.school.model.Student;
import com.school.school.model.User;
import com.school.school.model.dto.attendance.AttendanceRequest;
import com.school.school.repository.AttendanceRepository;
import com.school.school.repository.ClassSessionRepository;
import com.school.school.repository.StudentRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({AttendanceService.class, AttendanceRecordingPersistenceTest.StubGuards.class})
class AttendanceRecordingPersistenceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassSessionRepository classSessionRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private OwnershipGuard<Student> studentOwnershipGuard;
    @Autowired
    private OwnershipGuard<ClassSession> classSessionOwnershipGuard;

    @TestConfiguration
    static class StubGuards {

        @Bean
        OwnershipGuard<Student> studentOwnershipGuard() {
            return mock(OwnershipGuard.class);
        }

        @Bean
        OwnershipGuard<ClassSession> classSessionOwnershipGuard() {
            return mock(OwnershipGuard.class);
        }
    }

    @AfterEach
    void wipeTables() {
        attendanceRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        classSessionRepository.deleteAllInBatch();
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

    @Test
    @DisplayName("A mid-session denial rolls back the whole recording, leaving no partial rows")
    void midSessionDenialLeavesNoPartialRows() {
        User owner = persistedOwner();
        UserAuthenticated principal = principalOf(owner);
        ClassSession session = classSessionRepository.saveAndFlush(ClassSession.builder().user(owner).build());
        Student enrolled = studentRepository.saveAndFlush(Student.builder()
                .name("Enrolled")
                .dateOfBirth(LocalDate.of(2010, 5, 1))
                .user(owner)
                .build());
        long foreignStudentId = 9999L;

        when(classSessionOwnershipGuard.resolve(session.getId(), principal)).thenReturn(session);
        when(studentOwnershipGuard.resolve(enrolled.getId(), principal)).thenReturn(enrolled);
        when(studentOwnershipGuard.resolve(foreignStudentId, principal))
                .thenThrow(new AccessDeniedException("You do not have permission to access this student"));

        Map<Long, Boolean> studentsPresence = new LinkedHashMap<>();
        studentsPresence.put(enrolled.getId(), true);
        studentsPresence.put(foreignStudentId, false);
        AttendanceRequest request = new AttendanceRequest();
        request.setClassSessionId(session.getId());
        request.setStudentsPresence(studentsPresence);

        assertThatThrownBy(() -> attendanceService.markAttendanceList(request, principal))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(attendanceRepository.count()).isZero();
    }

    @Test
    @DisplayName("The schema rejects a second attendance row for the same student and session")
    void schemaRejectsDuplicatePair() {
        User owner = persistedOwner();
        ClassSession session = classSessionRepository.saveAndFlush(ClassSession.builder().user(owner).build());
        Student student = studentRepository.saveAndFlush(Student.builder()
                .name("Bob")
                .dateOfBirth(LocalDate.of(2010, 5, 1))
                .user(owner)
                .build());

        attendanceRepository.saveAndFlush(Attendance.builder()
                .student(student)
                .classSession(session)
                .present(true)
                .build());

        assertThatThrownBy(() -> attendanceRepository.saveAndFlush(Attendance.builder()
                .student(student)
                .classSession(session)
                .present(false)
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(attendanceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Re-marking persists as an update: same single row, flipped value")
    void reMarkPersistsAsUpdateOfSameRow() {
        User owner = persistedOwner();
        UserAuthenticated principal = principalOf(owner);
        ClassSession session = classSessionRepository.saveAndFlush(ClassSession.builder().user(owner).build());
        Student student = studentRepository.saveAndFlush(Student.builder()
                .name("Bob")
                .dateOfBirth(LocalDate.of(2010, 5, 1))
                .user(owner)
                .build());
        attendanceRepository.saveAndFlush(Attendance.builder()
                .student(student)
                .classSession(session)
                .present(true)
                .build());

        when(classSessionOwnershipGuard.resolve(session.getId(), principal)).thenReturn(session);
        when(studentOwnershipGuard.resolve(student.getId(), principal)).thenReturn(student);

        attendanceService.markAttendance(student.getId(), session.getId(), false, principal);

        assertThat(attendanceRepository.count()).isEqualTo(1);
        Attendance stored = attendanceRepository.findAll().get(0);
        assertThat(stored.getStudent().getId()).isEqualTo(student.getId());
        assertThat(stored.isPresent()).isFalse();
    }

}
