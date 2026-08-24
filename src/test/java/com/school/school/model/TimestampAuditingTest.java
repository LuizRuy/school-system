package com.school.school.model;

import com.school.school.infra.time.JpaAuditingConfig;
import com.school.school.model.enums.Status;
import com.school.school.repository.ClassroomRepository;
import com.school.school.repository.ClassSessionRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.TaskRepository;
import com.school.school.repository.UserRepository;
import com.school.school.support.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, TimestampAuditingTest.FixedClockConfig.class})
class TimestampAuditingTest {

    private static final Instant BASE = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ClassSessionRepository classSessionRepository;

    @BeforeEach
    void resetClock() {
        FixedClockConfig.CLOCK.setTo(BASE);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(FixedClockConfig.CLOCK);
    }

    @TestConfiguration
    static class FixedClockConfig {

        static final MutableClock CLOCK = MutableClock.at(BASE.toString());

        @Bean
        java.time.Clock clock() {
            return CLOCK;
        }
    }

    private User newUser() {
        return User.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada-%s@example.com".formatted(UUID.randomUUID()))
                .password("hashed")
                .build();
    }

    private Student newStudent(User owner) {
        return Student.builder()
                .name("Bob")
                .dateOfBirth(LocalDate.of(2010, 5, 1))
                .user(owner)
                .build();
    }

    private Classroom newClassroom(User owner) {
        return Classroom.builder()
                .name("Math 101")
                .user(owner)
                .build();
    }

    @Test
    @DisplayName("creating a user stamps createdAt and updatedAt with zero caller involvement")
    void createUserIsStamped() {
        LocalDateTime t0 = now();

        User saved = userRepository.saveAndFlush(newUser());

        assertThat(saved.getCreatedAt()).isEqualTo(t0);
        assertThat(saved.getUpdatedAt()).isEqualTo(t0);
    }

    @Test
    @DisplayName("creating a student stamps createdAt and updatedAt with zero caller involvement")
    void createStudentIsStamped() {
        LocalDateTime t0 = now();
        User owner = userRepository.saveAndFlush(newUser());

        Student saved = studentRepository.saveAndFlush(newStudent(owner));

        assertThat(saved.getCreatedAt()).isEqualTo(t0);
        assertThat(saved.getUpdatedAt()).isEqualTo(t0);
    }

    @Test
    @DisplayName("creating a classroom stamps createdAt and updatedAt with zero caller involvement")
    void createClassroomIsStamped() {
        LocalDateTime t0 = now();
        User owner = userRepository.saveAndFlush(newUser());

        Classroom saved = classroomRepository.saveAndFlush(newClassroom(owner));

        assertThat(saved.getCreatedAt()).isEqualTo(t0);
        assertThat(saved.getUpdatedAt()).isEqualTo(t0);
    }

    @Test
    @DisplayName("creating a task stamps createdAt with zero caller involvement")
    void createTaskIsStamped() {
        LocalDateTime t0 = now();
        User owner = userRepository.saveAndFlush(newUser());

        Task saved = taskRepository.saveAndFlush(Task.builder()
                .name("Homework 1")
                .user(owner)
                .build());

        assertThat(saved.getCreatedAt()).isEqualTo(t0);
    }

    @Test
    @DisplayName("creating a class session stamps createdAt with zero caller involvement")
    void createClassSessionIsStamped() {
        LocalDateTime t0 = now();
        User owner = userRepository.saveAndFlush(newUser());

        ClassSession saved = classSessionRepository.saveAndFlush(ClassSession.builder()
                .user(owner)
                .build());

        assertThat(saved.getCreatedAt()).isEqualTo(t0);
    }

    @Test
    @DisplayName("updating a user refreshes updatedAt even when the caller forgets it, and keeps createdAt")
    void updateUserRefreshesUpdatedAtWithoutCallerStamping() {
        LocalDateTime t0 = now();
        User user = userRepository.saveAndFlush(newUser());

        FixedClockConfig.CLOCK.advanceBy(Duration.ofHours(2));
        LocalDateTime t1 = now();

        user.setStatus(Status.DISABLED);
        userRepository.saveAndFlush(user);

        assertThat(user.getCreatedAt()).isEqualTo(t0);
        assertThat(user.getUpdatedAt()).isEqualTo(t1);
    }

    @Test
    @DisplayName("updating a student refreshes updatedAt even when the caller forgets it, and keeps createdAt")
    void updateStudentRefreshesUpdatedAtWithoutCallerStamping() {
        LocalDateTime t0 = now();
        User owner = userRepository.saveAndFlush(newUser());
        Student student = studentRepository.saveAndFlush(newStudent(owner));

        FixedClockConfig.CLOCK.advanceBy(Duration.ofHours(2));
        LocalDateTime t1 = now();

        student.setName("Bobby");
        studentRepository.saveAndFlush(student);

        assertThat(student.getCreatedAt()).isEqualTo(t0);
        assertThat(student.getUpdatedAt()).isEqualTo(t1);
    }

    @Test
    @DisplayName("updating a classroom refreshes updatedAt even when the caller forgets it, and keeps createdAt")
    void updateClassroomRefreshesUpdatedAtWithoutCallerStamping() {
        LocalDateTime t0 = now();
        User owner = userRepository.saveAndFlush(newUser());
        Classroom classroom = classroomRepository.saveAndFlush(newClassroom(owner));

        FixedClockConfig.CLOCK.advanceBy(Duration.ofHours(2));
        LocalDateTime t1 = now();

        classroom.setName("Math 102");
        classroomRepository.saveAndFlush(classroom);

        assertThat(classroom.getCreatedAt()).isEqualTo(t0);
        assertThat(classroom.getUpdatedAt()).isEqualTo(t1);
    }
}
