package com.school.school.repository;

import com.school.school.model.Submission;
import com.school.school.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByTask(Task task);
}
