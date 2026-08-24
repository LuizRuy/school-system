package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Attendance;
import com.school.school.model.ClassSession;
import com.school.school.model.Student;
import com.school.school.model.dto.attendance.AttendanceRequest;
import com.school.school.model.dto.attendance.UpdateAttendanceRequest;
import com.school.school.repository.AttendanceRepository;
import com.school.school.service.ownership.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final OwnershipGuard<Student> studentOwnershipGuard;
    private final OwnershipGuard<ClassSession> classSessionOwnershipGuard;

    @Transactional
    public void markAttendance(Long studentId, Long classSessionId, boolean present, UserAuthenticated userAuthenticated) {
        ClassSession classSession = classSessionOwnershipGuard.resolve(classSessionId, userAuthenticated);
        Student student = studentOwnershipGuard.resolve(studentId, userAuthenticated);

        record(student, classSession, present);
    }

    @Transactional
    public void markAttendanceList(AttendanceRequest attendanceRequest, UserAuthenticated userAuthenticated) {
        ClassSession classSession = classSessionOwnershipGuard.resolve(attendanceRequest.getClassSessionId(), userAuthenticated);

        attendanceRequest.getStudentsPresence()
                .forEach((studentId, present) ->
                        record(studentOwnershipGuard.resolve(studentId, userAuthenticated), classSession, present));
    }

    @Transactional
    public void updateAttendanceList(UpdateAttendanceRequest updateAttendanceRequest, UserAuthenticated userAuthenticated) {
        classSessionOwnershipGuard.authorize(updateAttendanceRequest.getClassSessionId(), userAuthenticated);
        studentOwnershipGuard.authorize(updateAttendanceRequest.getStudentId(), userAuthenticated);

        Attendance attendance = attendanceRepository.findByStudentIdAndClassSessionId(updateAttendanceRequest.getStudentId(), updateAttendanceRequest.getClassSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Attendance record not found for student ID " + updateAttendanceRequest.getStudentId() +
                        " and class session ID " + updateAttendanceRequest.getClassSessionId()));

        attendance.setPresent(updateAttendanceRequest.getPresence());
        attendanceRepository.save(attendance);
    }

    private void record(Student student, ClassSession classSession, boolean present) {
        attendanceRepository.findByStudentIdAndClassSessionId(student.getId(), classSession.getId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setPresent(present);
                            attendanceRepository.save(existing);
                        },
                        () -> attendanceRepository.save(Attendance.builder()
                                .student(student)
                                .classSession(classSession)
                                .present(present)
                                .build()));
    }

}
