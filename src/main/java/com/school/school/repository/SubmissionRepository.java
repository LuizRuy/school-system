package com.school.school.repository;

import com.school.school.model.Submission;
import com.school.school.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByTask(Task task);
    Optional<Submission> findByTaskIdAndStudentId(Long taskId, Long studentId);
}
