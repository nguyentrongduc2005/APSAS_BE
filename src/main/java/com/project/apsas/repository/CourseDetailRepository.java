package com.project.apsas.repository;

import com.project.apsas.repository.projection.CourseDetailRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@org.springframework.stereotype.Repository
public interface CourseDetailRepository
        extends org.springframework.data.repository.Repository<com.project.apsas.entity.Course, Long> {

    @Query(value =
            "SELECT " +
                    " c.id AS id, c.name AS name, c.code AS code, c.visibility AS visibility, c.created_at AS createdAt, " +
                    " (SELECT u.id FROM enrollments e JOIN users u ON u.id = e.user_id " +
                    "   WHERE e.course_id = c.id AND e.role IN ('TEACHER','OWNER') " +
                    "   ORDER BY (e.role='TEACHER') DESC, u.id ASC LIMIT 1) AS instructorId, " +
                    " (SELECT u.name FROM enrollments e JOIN users u ON u.id = e.user_id " +
                    "   WHERE e.course_id = c.id AND e.role IN ('TEACHER','OWNER') " +
                    "   ORDER BY (e.role='TEACHER') DESC, u.id ASC LIMIT 1) AS instructorName, " +
                    " (SELECT u.email FROM enrollments e JOIN users u ON u.id = e.user_id " +
                    "   WHERE e.course_id = c.id AND e.role IN ('TEACHER','OWNER') " +
                    "   ORDER BY (e.role='TEACHER') DESC, u.id ASC LIMIT 1) AS instructorEmail, " +
                    " NULL AS instructorAvatar, " + /* nếu có cột avatar thì sửa: u.avatar AS instructorAvatar */
                    " (SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.id AND e.role = 'STUDENT') AS studentsCount, " +
                    " (SELECT COUNT(*) FROM courses_contents cc WHERE cc.courses_id = c.id) AS lessonsCount " +
                    "FROM courses c " +
                    "WHERE c.id = :courseId AND c.visibility = 'PUBLIC' " +
                    "LIMIT 1",
            nativeQuery = true)
    CourseDetailRow findPublicDetail(@Param("courseId") Long courseId);

    // Lấy top 5 lesson (nếu UI cần)
    @Query(value =
            "SELECT cc.id, cc.title, cc.order_index " +
                    "FROM courses_contents cc " +
                    "WHERE cc.courses_id = :courseId " +
                    "ORDER BY cc.order_index ASC, cc.id ASC " +
                    "LIMIT 5",
            nativeQuery = true)
    java.util.List<Object[]> findTopLessons(@Param("courseId") Long courseId);
}
