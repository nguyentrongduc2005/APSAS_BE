package com.project.apsas.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.apsas.entity.assignment_evaluation_maps;

@Repository
public interface assignmentEvaluationMapsRepository extends JpaRepository<assignment_evaluation_maps, assignment_evaluation_maps.PK> {

}
