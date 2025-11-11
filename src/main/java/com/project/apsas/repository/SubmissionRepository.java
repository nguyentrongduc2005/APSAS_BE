package com.project.apsas.repository;

import com.project.apsas.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}
