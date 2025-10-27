package com.project.apsas.repository;

import com.project.apsas.entity.Assignment;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

}
