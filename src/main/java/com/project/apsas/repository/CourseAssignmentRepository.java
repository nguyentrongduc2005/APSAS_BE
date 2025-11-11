package com.project.apsas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.apsas.entity.CourseAssignment;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, CourseAssignment.PK> {

    @Query("SELECT COUNT(ca) FROM CourseAssignment ca WHERE ca.course.id = :courseId")
    int countByCourseId(@Param("courseId") Long courseId);
}
