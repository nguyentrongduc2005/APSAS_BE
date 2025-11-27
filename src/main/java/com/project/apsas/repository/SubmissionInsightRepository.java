package com.project.apsas.repository;

import com.project.apsas.dto.student.StudentCourseInsightDto;
import com.project.apsas.dto.teacher.TeacherCourseInsightDto;
import com.project.apsas.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubmissionInsightRepository extends JpaRepository<Submission, Long> {

    @Query("""
        select new com.project.apsas.dto.student.StudentCourseInsightDto(
            c.id,
            c.code,
            c.name,
            count(distinct s.assignmentId),
            count(s),
            coalesce(avg(s.score), 0)
        )
        from Submission s, Course c
        where s.userId = :studentId
          and c.id = s.courseId
        group by c.id, c.code, c.name
        """)
    List<StudentCourseInsightDto> findStudentCourseInsights(Long studentId);

    @Query("""
        select new com.project.apsas.dto.teacher.TeacherCourseInsightDto(
            c.id,
            c.code,
            c.name,
            count(distinct s.userId),
            count(distinct s.assignmentId),
            count(s),
            coalesce(avg(s.score), 0)
        )
        from Submission s, Course c
        where c.id = s.courseId
          and c.creator.id = :teacherId
        group by c.id, c.code, c.name
        """)
    List<TeacherCourseInsightDto> findTeacherCourseInsights(Long teacherId);
}
