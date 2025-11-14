package com.project.apsas.repository;

import com.project.apsas.entity.TeacherFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherFeedbackRepository extends JpaRepository<TeacherFeedback, Long> {

    List<TeacherFeedback> findBySubmissionId(Long submissionId);
}
