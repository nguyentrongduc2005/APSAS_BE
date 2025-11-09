package com.project.apsas.repository;

import com.project.apsas.repository.projection.PublicCourseRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.stereotype.Repository;
@Repository
public interface CoursePublicRepository
        extends org.springframework.data.repository.Repository<com.project.apsas.entity.Course, Long> {

    @Query(value = """
        SELECT 
            c.id                           AS id,
            c.name                         AS name,
            c.code                         AS code,
            (SELECT u.id FROM enrollments e
             JOIN users u ON u.id = e.user_id
             WHERE e.course_id = c.id AND e.role IN ('TEACHER','OWNER')
             ORDER BY (e.role = 'TEACHER') DESC, u.id ASC LIMIT 1) AS instructorId,
            (SELECT u.name FROM enrollments e
             JOIN users u ON u.id = e.user_id
             WHERE e.course_id = c.id AND e.role IN ('TEACHER','OWNER')
             ORDER BY (e.role = 'TEACHER') DESC, u.id ASC LIMIT 1) AS instructorName,
            (SELECT COUNT(*) FROM enrollments e
             WHERE e.course_id = c.id AND e.role = 'STUDENT') AS studentsCount,
            (SELECT COUNT(*) FROM courses_contents cc
             WHERE cc.courses_id = c.id) AS lessonsCount,
            c.created_at                    AS createdAt,
            c.visibility                    AS visibility
        FROM courses c
        WHERE c.visibility = 'PUBLIC'
          AND (
             :kw = '' OR
             c.name LIKE :likeKw OR
             c.code LIKE :likeKw OR
             EXISTS (
                 SELECT 1
                 FROM enrollments e
                 JOIN users u ON u.id = e.user_id
                 WHERE e.course_id = c.id AND e.role IN ('TEACHER','OWNER') AND u.name LIKE :likeKw
             )
          )
        ORDER BY c.created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<PublicCourseRow> findPublicCourses(
            @Param("kw") String kw,
            @Param("likeKw") String likeKw,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM courses c
        WHERE c.visibility = 'PUBLIC'
          AND (
             :kw = '' OR
             c.name LIKE :likeKw OR
             c.code LIKE :likeKw OR
             EXISTS (
                 SELECT 1
                 FROM enrollments e
                 JOIN users u ON u.id = e.user_id
                 WHERE e.course_id = c.id AND e.role IN ('TEACHER','OWNER') AND u.name LIKE :likeKw
             )
          )
        """, nativeQuery = true)
    long countPublicCourses(@Param("kw") String kw, @Param("likeKw") String likeKw);
}
