package com.school.school.repository;

import com.school.school.model.Student;
import com.school.school.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByIdAndUser(Long id, User user);
    List<Student> findByUser(User user);
}
