package com.school.school.repository;

import com.school.school.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndClassSessionId(Long studentId, Long classSessionId);
}
