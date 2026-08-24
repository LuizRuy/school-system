package com.school.school.repository;

import com.school.school.model.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    List<ClassSession> findByUserId(Long userId);

    @Query("select cs.user.id from ClassSession cs where cs.id = :id")
    Optional<Long> findOwnerIdById(Long id);
}
