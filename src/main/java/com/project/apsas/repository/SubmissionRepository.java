package com.project.apsas.repository;



    // ====== KPI THEO SINH VIÊN TRONG 1 KHÓA ======

   
import com.project.apsas.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
//     @Query("""
//        select count(s)
//        from Submission s
//        where s.assignment.course.id = :courseId
//          and s.user.id = :userId
//    """)
//    int countByCourseIdAndUserId(@Param("courseId") Long courseId,
//                                 @Param("userId") Long userId);
//
//    @Query("""
//        select count(s)
//        from Submission s
//        where s.assignment.course.id = :courseId
//          and s.user.id = :userId
//          and s.score is not null
//    """)
//    int countGradedByCourseIdAndUserId(@Param("courseId") Long courseId,
//                                       @Param("userId") Long userId);
//
//    @Query("""
//        select avg(s.score)
//        from Submission s
//        where s.assignment.course.id = :courseId
//          and s.user.id = :userId
//          and s.score is not null
//    """)
//    Double avgScoreByCourseIdAndUserId(@Param("courseId") Long courseId,
//                                       @Param("userId") Long userId);
    /**
     * Lấy tất cả submissions của một assignment
     */
    List<Submission> findByAssignmentId(Long assignmentId);

    /**
     * Lấy submissions của một user cho một assignment
     */
    Optional<Submission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    /**
     * Lấy tất cả submissions của một user
     */
    List<Submission> findByUserId(Long userId);

    /**
     * Lấy submissions của assignment trong course với pagination
     * Query để giáo viên xem tất cả submission của học sinh trong một course
     */
    @Query(value = """
        SELECT s.* FROM submissions s
        JOIN assignments a ON s.assignment_id = a.id
        JOIN courses_assignments ca ON a.id = ca.assignment_id
        WHERE ca.course_id = :courseId
        ORDER BY s.submitted_at DESC
        """, nativeQuery = true)
    Page<Submission> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    /**
     * Lấy submissions của một assignment trong course
     */
    @Query(value = """
        SELECT s.* FROM submissions s
        JOIN assignments a ON s.assignment_id = a.id
        WHERE a.id = :assignmentId AND a.id IN (
            SELECT assignment_id FROM courses_assignments WHERE course_id = :courseId
        )
        ORDER BY s.submitted_at DESC
        """, nativeQuery = true)
    Page<Submission> findByAssignmentIdAndCourseId(
            @Param("assignmentId") Long assignmentId,
            @Param("courseId") Long courseId,
            Pageable pageable
    );

    /**
     * Lấy submission details của một student cho một assignment
     */
    @Query(value = """
        SELECT s.id, s.assignment_id, s.user_id, s.language, s.code, s.report_json,
               s.score, s.feedback, s.passed, s.attempt_no, s.submitted_at,
               u.name as student_name, u.email as student_email,
               a.title as assignment_title
        FROM submissions s
        JOIN users u ON s.user_id = u.id
        JOIN assignments a ON s.assignment_id = a.id
        WHERE s.assignment_id = :assignmentId AND s.user_id = :userId
        """, nativeQuery = true)
    Optional<Object[]> findDetailedSubmission(
            @Param("assignmentId") Long assignmentId,
            @Param("userId") Long userId
    );

    /**
     * Lấy danh sách sinh viên đã nộp bài trong một assignment của course
     * Hiển thị thông tin: studentId, studentName, studentEmail, score, passed, submittedAt, attemptNo
     */
    @Query(value = """
        SELECT 
            u.id as studentId,
            u.name as studentName,
            u.email as studentEmail,
            s.score,
            s.passed,
            s.submitted_at as submittedAt,
            s.attempt_no as attemptNo,
            a.title as assignmentTitle
        FROM submissions s
        JOIN users u ON s.user_id = u.id
        JOIN assignments a ON s.assignment_id = a.id
        WHERE a.id = :assignmentId 
          AND a.id IN (
              SELECT assignment_id FROM courses_assignments WHERE course_id = :courseId
          )
        GROUP BY u.id
        ORDER BY s.submitted_at DESC
        """, nativeQuery = true)
    Page<Object[]> findStudentSubmissionsByCourseAndAssignment(
            @Param("courseId") Long courseId,
            @Param("assignmentId") Long assignmentId,
            Pageable pageable
    );
}
