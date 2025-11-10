package com.project.apsas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.apsas.entity.Course;
import com.project.apsas.enums.CourseVisibility;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
        select c from Course c
        where
          (
            (:visibility is null and c.visibility = com.project.apsas.enums.CourseVisibility.PUBLIC)
            or (:visibility is not null and c.visibility = :visibility)
          )
          and (:keyword is null
               or lower(c.name) like lower(concat('%', :keyword, '%'))
               or lower(c.code) like lower(concat('%', :keyword, '%')))
        """)
    Page<Course> searchPublic(@Param("keyword") String keyword,
                              @Param("visibility") CourseVisibility visibility,
                              Pageable pageable);

    @Query("""
        select new com.project.apsas.dto.teacher.TeacherCourseSummaryResponse(
            c.id, c.code, c.name, c.visibility,
            (select count(a) from CourseAssignment a where a.course.id = c.id),
            (select avg(s.score) from Submission s where s.course.id = c.id)
        )
        from Course c
        where (:teacherId is null or c.teacher.id = :teacherId)
        """)
    java.util.List<com.project.apsas.dto.teacher.TeacherCourseSummaryResponse>
    findSummariesByTeacherId(@Param("teacherId") Long teacherId);
}
