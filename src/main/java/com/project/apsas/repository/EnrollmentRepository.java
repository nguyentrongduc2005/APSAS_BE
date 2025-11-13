package com.project.apsas.repository;

import com.project.apsas.entity.Enrollment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Enrollment.PK> {
    @Query("SELECT e.course.id, COUNT(e), e.course.limit FROM Enrollment e WHERE e.course.id IN :courseIds GROUP BY e.course.id, e.course.limit")
    List<Object[]> findStudentCountsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COUNT(DISTINCT e.user.id) " +
            "FROM Enrollment e JOIN e.course c " +
            "WHERE c.creator.id = :creatorId")
    Long countTotalStudentsByCreatorId(@Param("creatorId") Long creatorId);
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :studentId")
    long countByStudentId(@Param("studentId") Long studentId);
}
