package com.project.apsas.repository;

import com.project.apsas.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Lấy danh sách feedback theo submission, sắp xếp theo thời gian tạo
    List<Feedback> findBySubmissionIdOrderByCreatedAtAsc(Long submissionId);
}
