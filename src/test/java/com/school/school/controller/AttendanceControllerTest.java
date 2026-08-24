package com.school.school.controller;

import com.school.school.infra.security.CustomAccessDeniedHandler;
import com.school.school.infra.security.CustomAuthenticationEntryPoint;
import com.school.school.infra.security.OpenEndpointRateLimitFilter;
import com.school.school.infra.security.SecurityConfig;
import com.school.school.infra.security.TokenAuthenticationFilter;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Attendance;
import com.school.school.model.ClassSession;
import com.school.school.model.Student;
import com.school.school.model.User;
import com.school.school.repository.AttendanceRepository;
import com.school.school.repository.ClassSessionRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.service.AttendanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@Import({SecurityConfig.class, AttendanceControllerTest.SliceSecurityBeans.class,
        AttendanceControllerTest.RealGuards.class, AttendanceService.class})
class AttendanceControllerTest {

    private static final long TEACHER_ID = 7L;
    private static final long OTHER_TEACHER_ID = 8L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceRepository attendanceRepository;

    @MockitoBean
    private StudentRepository studentRepository;

    @MockitoBean
    private ClassSessionRepository classSessionRepository;

    @TestConfiguration
    static class SliceSecurityBeans {

        @Bean
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
            return new CustomAuthenticationEntryPoint();
        }

        @Bean
        CustomAccessDeniedHandler customAccessDeniedHandler() {
            return new CustomAccessDeniedHandler();
        }

        @Bean
        OpenEndpointRateLimitFilter openEndpointRateLimitFilter() {
            return new OpenEndpointRateLimitFilter(Clock.systemUTC());
        }

        @Bean
        TokenAuthenticationFilter tokenAuthenticationFilter(CustomAuthenticationEntryPoint entryPoint) {
            return new TokenAuthenticationFilter(null, null, entryPoint);
        }
    }

    @TestConfiguration
    static class RealGuards {

        @Bean
        com.school.school.service.ownership.OwnershipGuard<Student> studentOwnershipGuard(StudentRepository studentRepository) {
            return com.school.school.service.ownership.OwnershipGuards.forStudents(studentRepository);
        }

        @Bean
        com.school.school.service.ownership.OwnershipGuard<ClassSession> classSessionOwnershipGuard(ClassSessionRepository classSessionRepository) {
            return com.school.school.service.ownership.OwnershipGuards.forClassSessions(classSessionRepository);
        }
    }

    private Authentication asTeacher(long userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("teacher-%d@school.test".formatted(userId));
        return new UsernamePasswordAuthenticationToken(new UserAuthenticated(user), null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
    }

    private Student ownedStudent(long studentId, long ownerId) {
        User user = new User();
        user.setId(ownerId);
        Student student = new Student();
        student.setId(studentId);
        student.setUser(user);
        return student;
    }

    private ClassSession ownedSession(long sessionId, long ownerId) {
        User user = new User();
        user.setId(ownerId);
        ClassSession classSession = new ClassSession();
        classSession.setId(sessionId);
        classSession.setUser(user);
        return classSession;
    }

    private Attendance markedRow(long id, long studentId, long sessionId, boolean present) {
        return Attendance.builder()
                .id(id)
                .student(ownedStudent(studentId, TEACHER_ID))
                .classSession(ownedSession(sessionId, TEACHER_ID))
                .present(present)
                .build();
    }

    @Test
    @DisplayName("A fresh mark returns 201 and inserts one attendance row")
    void freshMarkReturns201AndInsertsRow() throws Exception {
        when(classSessionRepository.findById(31L)).thenReturn(Optional.of(ownedSession(31L, TEACHER_ID)));
        when(studentRepository.findById(21L)).thenReturn(Optional.of(ownedStudent(21L, TEACHER_ID)));
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/attendances/31/21/true")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(asTeacher(TEACHER_ID))))
                .andExpect(status().isCreated());

        ArgumentCaptor<Attendance> saved = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isNull();
        assertThat(saved.getValue().getStudent().getId()).isEqualTo(21L);
        assertThat(saved.getValue().getClassSession().getId()).isEqualTo(31L);
        assertThat(saved.getValue().isPresent()).isTrue();
    }

    @Test
    @DisplayName("Re-marking an already-marked pair returns 201 and flips the existing row instead of duplicating it")
    void reMarkUpdatesExistingRowInsteadOfDuplicatingIt() throws Exception {
        when(classSessionRepository.findById(31L)).thenReturn(Optional.of(ownedSession(31L, TEACHER_ID)));
        when(studentRepository.findById(21L)).thenReturn(Optional.of(ownedStudent(21L, TEACHER_ID)));
        Attendance existing = markedRow(5L, 21L, 31L, true);
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.of(existing));

        mockMvc.perform(post("/api/v1/attendances/31/21/false")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(asTeacher(TEACHER_ID))))
                .andExpect(status().isCreated());

        verify(attendanceRepository).save(same(existing));
        assertThat(existing.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Whole-session recording returns 201 and upserts each student")
    void wholeSessionRecordingUpsertsEachStudent() throws Exception {
        when(classSessionRepository.findById(31L)).thenReturn(Optional.of(ownedSession(31L, TEACHER_ID)));
        when(studentRepository.findById(21L)).thenReturn(Optional.of(ownedStudent(21L, TEACHER_ID)));
        when(studentRepository.findById(22L)).thenReturn(Optional.of(ownedStudent(22L, TEACHER_ID)));
        Attendance alreadyMarked = markedRow(5L, 21L, 31L, true);
        when(attendanceRepository.findByStudentIdAndClassSessionId(21L, 31L)).thenReturn(Optional.of(alreadyMarked));
        when(attendanceRepository.findByStudentIdAndClassSessionId(22L, 31L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/attendances")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(asTeacher(TEACHER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"classSessionId":31,"studentsPresence":{"21":false,"22":true}}
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<Attendance> saved = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository, org.mockito.Mockito.times(2)).save(saved.capture());
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
    @DisplayName("A foreign student denies recording as 403 and writes nothing")
    void foreignStudentIsDeniedAs403() throws Exception {
        when(classSessionRepository.findById(31L)).thenReturn(Optional.of(ownedSession(31L, TEACHER_ID)));
        when(studentRepository.findById(21L)).thenReturn(Optional.of(ownedStudent(21L, OTHER_TEACHER_ID)));

        mockMvc.perform(post("/api/v1/attendances/31/21/true")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(asTeacher(TEACHER_ID))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(attendanceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("A foreign class session is denied as 404 and writes nothing")
    void foreignSessionIsDeniedAs404() throws Exception {
        when(classSessionRepository.findById(31L)).thenReturn(Optional.of(ownedSession(31L, OTHER_TEACHER_ID)));
        when(studentRepository.findById(21L)).thenReturn(Optional.of(ownedStudent(21L, TEACHER_ID)));

        mockMvc.perform(post("/api/v1/attendances/31/21/true")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(asTeacher(TEACHER_ID))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Class session not found with id: 31"));

        verify(attendanceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("An unknown class session keeps 404")
    void unknownSessionKeeps404() throws Exception {
        when(classSessionRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/attendances/99/21/true")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(asTeacher(TEACHER_ID))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Class session not found with id: 99"));
    }

}
