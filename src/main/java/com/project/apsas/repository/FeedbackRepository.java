package com.project.apsas.repository;

import com.project.apsas.entity.Feedback;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

}
