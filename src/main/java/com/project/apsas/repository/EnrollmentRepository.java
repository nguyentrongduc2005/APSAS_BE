package com.project.apsas.repository;

import com.project.apsas.entity.Enrollment;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Enrollment.PK> {

}
