package com.project.apsas.repository;

import com.project.apsas.entity.Feedback;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Lấy tất cả feedback của 1 submission, sort theo thời gian mới nhất trước
    List<Feedback> findAllBySubmissionIdOrderByCreatedAtDesc(Long submissionId);

}
