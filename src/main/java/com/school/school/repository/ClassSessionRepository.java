package com.school.school.repository;

import com.school.school.model.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    List<ClassSession> findByUserId(Long userId);
}
