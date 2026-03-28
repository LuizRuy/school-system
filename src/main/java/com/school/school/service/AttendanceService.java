package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Attendance;
import com.school.school.model.ClassSession;
import com.school.school.model.Student;
import com.school.school.model.dto.attendance.AttendanceRequest;
import com.school.school.model.dto.attendance.UpdateAttendanceRequest;
import com.school.school.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;
    private final ClassSessionService classSessionService;

    public void markAttendance(Long studentId, Long classSessionId, boolean present, UserAuthenticated userAuthenticated) {
        ClassSession classSession = classSessionService.getClassSessionById(classSessionId, userAuthenticated);

        Student student = studentService.findStudent(studentId, userAuthenticated);

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setClassSession(classSession);
        attendance.setPresent(present);

        attendanceRepository.save(attendance);
    }

    @Transactional
    public void markAttendanceList(AttendanceRequest attendanceRequest, UserAuthenticated userAuthenticated) {

        ClassSession classSession = classSessionService.getClassSessionById(attendanceRequest.getClassSessionId(), userAuthenticated);

        List<Attendance> attendances = attendanceRequest.getStudentsPresence().entrySet()
                .stream()
                .map(entry -> {
                    Attendance attendance = new Attendance();
                    attendance.setClassSession(classSession);
                    attendance.setStudent(
                            studentService.findStudent(entry.getKey(), userAuthenticated)
                    );
                    attendance.setPresent(entry.getValue());
                    return attendance;
                })
                .toList();

        attendanceRepository.saveAll(attendances);

    }

    public void updateAttendanceList(UpdateAttendanceRequest updateAttendanceRequest, UserAuthenticated userAuthenticated) {
        classSessionService.getClassSessionById(updateAttendanceRequest.getClassSessionId(), userAuthenticated);
        studentService.findStudent(updateAttendanceRequest.getStudentId(), userAuthenticated);

        Attendance attendance = attendanceRepository.findByStudentIdAndClassSessionId(updateAttendanceRequest.getStudentId(), updateAttendanceRequest.getClassSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Attendance record not found for student ID " + updateAttendanceRequest.getStudentId() +
                        " and class session ID " + updateAttendanceRequest.getClassSessionId()));

        attendance.setPresent(updateAttendanceRequest.getPresence());
        attendanceRepository.save(attendance);
    }

}
