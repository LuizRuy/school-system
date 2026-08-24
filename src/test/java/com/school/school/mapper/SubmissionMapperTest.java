package com.school.school.mapper;

import com.school.school.model.Student;
import com.school.school.model.Submission;
import com.school.school.model.Task;
import com.school.school.model.User;
import com.school.school.model.dto.submission.StudentSubmission;
import com.school.school.model.dto.submission.SubmissionRequest;
import com.school.school.model.dto.submission.SubmissionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionMapperTest {

    private final SubmissionMapper submissionMapper = new SubmissionMapper();

    private static User userWithId(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private static Student student(long studentId, String name) {
        return Student.builder()
                .id(studentId)
                .name(name)
                .user(userWithId(7L))
                .build();
    }

    private static Task task(long taskId) {
        Task task = Task.builder()
                .id(taskId)
                .name("Algebra homework")
                .user(userWithId(7L))
                .build();
        task.setCreatedAt(LocalDateTime.of(2026, 8, 24, 9, 0));
        return task;
    }

    @Test
    @DisplayName("The mapper is constructed with no collaborators at all")
    void mapperNeedsNoCollaborators() {
        assertThat(submissionMapper).isNotNull();
    }

    @Test
    @DisplayName("Batch intake maps mixed submitted states onto already-resolved students")
    void batchIntakeMapsMixedStatesOntoResolvedStudents() {
        Student alice = student(21L, "Alice");
        Student bob = student(22L, "Bob");
        Task algebra = task(31L);
        Map<Long, Boolean> states = new LinkedHashMap<>();
        states.put(21L, true);
        states.put(22L, false);
        Map<Long, Student> resolvedStudents = Map.of(21L, alice, 22L, bob);

        List<Submission> submissions = submissionMapper.toSubmissions(states, algebra, resolvedStudents);

        assertThat(submissions)
                .hasSize(2)
                .satisfiesExactly(
                        submission -> {
                            assertThat(submission.getStudent()).isSameAs(alice);
                            assertThat(submission.getTask()).isSameAs(algebra);
                            assertThat(submission.getSubmitted()).isTrue();
                            assertThat(submission.getId()).isNull();
                        },
                        submission -> {
                            assertThat(submission.getStudent()).isSameAs(bob);
                            assertThat(submission.getTask()).isSameAs(algebra);
                            assertThat(submission.getSubmitted()).isFalse();
                        });
    }

    @Test
    @DisplayName("A single request maps onto the given student and task")
    void singleRequestMapsOntoGivenStudentAndTask() {
        SubmissionRequest request = new SubmissionRequest();
        request.setStudentId(21L);
        request.setTaskId(31L);
        request.setSubmitted(true);

        Submission submission = submissionMapper.toEntity(request, student(21L, "Alice"), task(31L));

        assertThat(submission.getStudent().getId()).isEqualTo(21L);
        assertThat(submission.getTask().getId()).isEqualTo(31L);
        assertThat(submission.getSubmitted()).isTrue();
    }

    @Test
    @DisplayName("A submission response carries the task summary and per-student rows")
    void responseCarriesTaskSummaryAndPerStudentRows() {
        Task algebra = task(31L);
        Submission first = Submission.builder()
                .student(student(21L, "Alice"))
                .task(algebra)
                .submitted(true)
                .build();
        Submission second = Submission.builder()
                .student(student(22L, "Bob"))
                .task(algebra)
                .submitted(false)
                .build();

        SubmissionResponse response = submissionMapper.toSubmissionResponse(algebra, List.of(first, second));

        assertThat(response.getTaskId()).isEqualTo(31L);
        assertThat(response.getTaskTitle()).isEqualTo("Algebra homework");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 8, 24, 9, 0));
        assertThat(response.getSubmissions())
                .extracting(StudentSubmission::getStudentId,
                            StudentSubmission::getStudentName,
                            StudentSubmission::getSubmitted)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(21L, "Alice", true),
                        org.assertj.core.groups.Tuple.tuple(22L, "Bob", false));
    }
}
