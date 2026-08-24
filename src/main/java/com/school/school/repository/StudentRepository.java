package com.school.school.repository;

import com.school.school.model.Classroom;
import com.school.school.model.Student;
import com.school.school.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface    StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByUser(User user);
    List<Student> findByClassrooms(Classroom classroom);

    @Query("select s.user.id from Student s where s.id = :id")
    Optional<Long> findOwnerIdById(Long id);
}
