package com.project.apsas.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.apsas.entity.AssignmentEvaluationMaps;

@Repository
public interface assignmentEvaluationMapsRepository extends JpaRepository<AssignmentEvaluationMaps, AssignmentEvaluationMaps.PK> {

}
