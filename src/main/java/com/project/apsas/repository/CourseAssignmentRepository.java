package com.project.apsas.repository;

import com.project.apsas.dto.request.assignment.AssignmentListItemDTO;
import com.project.apsas.dto.response.assignment.AssignmentDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.apsas.entity.CourseAssignment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, CourseAssignment.PK> {

    @Query("SELECT COUNT(ca) FROM CourseAssignment ca WHERE ca.course.id = :courseId")
    int countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT ca.course.id, COUNT(ca) FROM CourseAssignment ca WHERE ca.course.id IN :courseIds GROUP BY ca.course.id")
    List<Object[]> findAssignmentLessonsByCourseIds(@Param("courseIds") List<Long> courseIds);

    boolean existsCourseAssignmentByAssignmentIdAndCourseId(Long assignmentId, Long courseId);
    
    @Query("SELECT ca FROM CourseAssignment ca WHERE ca.course.id = :courseId")
    List<CourseAssignment> findAllByCourseId(@Param("courseId") Long courseId);

    @Query("""
    SELECT new com.project.apsas.dto.request.assignment.AssignmentListItemDTO(
        a.id,
        a.title,
        ca.dueAt
    )
    FROM CourseAssignment ca
    JOIN ca.assignment a
    WHERE ca.courseId = :courseId
    ORDER BY ca.dueAt ASC
    """)
    List<AssignmentListItemDTO> findAssignmentsByCourseId(@Param("courseId") Long courseId);


    @Query("""
    SELECT new com.project.apsas.dto.response.assignment.AssignmentDetailDTO(
        a.id,
        a.title,
        a.statementMd,
        a.maxScore,
        a.attemptsLimit,
        a.proficiency,
        a.orderNo,
        ca.openAt,
        ca.dueAt,
        s.name,
        t.title
    )
    FROM CourseAssignment ca
    JOIN ca.assignment a
    LEFT JOIN a.skill s
    LEFT JOIN a.tutorial t
    WHERE ca.courseId = :courseId AND ca.assignmentId = :assignmentId
    """)
    Optional<AssignmentDetailDTO> findAssignmentDetailByCourseIdAndAssignmentId(
            @Param("courseId") Long courseId,
            @Param("assignmentId") Long assignmentId
    );

}
