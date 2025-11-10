package com.project.apsas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.apsas.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // ====== KPI THEO SINH VIÊN TRONG 1 KHÓA ======

    @Query("""
        select count(s) 
        from Submission s 
        where s.assignment.course.id = :courseId 
          and s.user.id = :userId
    """)
    int countByCourseIdAndUserId(@Param("courseId") Long courseId,
                                 @Param("userId") Long userId);

    @Query("""
        select count(s) 
        from Submission s 
        where s.assignment.course.id = :courseId 
          and s.user.id = :userId
          and s.score is not null
    """)
    int countGradedByCourseIdAndUserId(@Param("courseId") Long courseId,
                                       @Param("userId") Long userId);

    @Query("""
        select avg(s.score) 
        from Submission s 
        where s.assignment.course.id = :courseId 
          and s.user.id = :userId
          and s.score is not null
    """)
    Double avgScoreByCourseIdAndUserId(@Param("courseId") Long courseId,
                                       @Param("userId") Long userId);
}
