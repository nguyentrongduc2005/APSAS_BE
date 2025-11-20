package com.project.apsas.repository;

import com.project.apsas.entity.Enrollment;
import com.project.apsas.enums.EnrollmentRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Enrollment.PK> {
    boolean existsEnrollmentByCourseIdAndUserId(Long courseId, Long userId);

    @Query("SELECT e.course.id, COUNT(e), e.course.limit FROM Enrollment e WHERE e.course.id IN :courseIds GROUP BY e.course.id, e.course.limit")
    List<Object[]> findStudentCountsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COUNT(DISTINCT e.user.id) " +
            "FROM Enrollment e JOIN e.course c " +
            "WHERE c.creator.id = :creatorId")
    Long countTotalStudentsByCreatorId(@Param("creatorId") Long creatorId);
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :studentId")
    long countByStudentId(@Param("studentId") Long studentId);

    @Query("""
    SELECT e.course.id
    FROM Enrollment e
    WHERE e.user.id = :studentId
    """)
    List<Long> findCourseIdsByStudent(@Param("studentId") Long studentId);

    @Query("""
    SELECT COUNT(DISTINCT e.course.id)
    FROM Enrollment e
    JOIN Submission s ON s.user.id = e.user.id AND s.status = 'COMPLETE'
    WHERE e.user.id = :studentId
""")
    Long countCompletedCoursesByStudent(@Param("studentId") Long studentId);

    @Query("""
    SELECT e
    FROM Enrollment e
    WHERE e.user.id = :userId
    """)
    List<Enrollment> findAllEnrollments(@Param("userId") Long userId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // Đếm số lượng học viên hiện tại trong khóa
    long countByCourseId(Long courseId);

    // Tìm tất cả enrollment của user với vai trò cụ thể
    List<Enrollment> findByUserIdAndRole(Long userId, EnrollmentRole role);
    
    // Tìm tất cả enrollment theo courseId và role
    List<Enrollment> findByCourseIdAndRole(Long courseId, EnrollmentRole role);

    @Query("SELECT COUNT(DISTINCT e.userId) FROM Enrollment e WHERE e.courseId IN :courseIds AND e.role = 'STUDENT'")
    int countStudentsByCourseIds(@Param("courseIds") List<Long> courseIds);



}
