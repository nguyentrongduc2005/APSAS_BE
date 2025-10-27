package com.project.apsas.repository;


import com.project.apsas.entity.AssignmentEvaluation;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AssignmentEvaluationRepository extends JpaRepository<AssignmentEvaluation, Long> {

}
