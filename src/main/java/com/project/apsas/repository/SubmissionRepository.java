package com.project.apsas.repository;



    // ====== KPI THEO SINH VIÊN TRONG 1 KHÓA ======

   
import com.project.apsas.dto.student.DailyScoreDTO;
import com.project.apsas.dto.student.ProgressDTO;
import com.project.apsas.entity.Assignment;
import com.project.apsas.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    int countByCourseIdAndAssignmentIdAndUserId(Long courseId, Long assignmentId, Long userId);

    @Query("""
    SELECT NEW com.project.apsas.dto.student.DailyScoreDTO(
        CAST(s.submittedAt AS DATE),
        AVG(s.score)
    )
    FROM Submission s
    WHERE s.userId = :userId
      AND s.submittedAt BETWEEN :fromDate AND :toDate
    GROUP BY CAST(s.submittedAt AS DATE)
    ORDER BY CAST(s.submittedAt AS DATE)
""")
    List<DailyScoreDTO> findScoresByDateRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );


    @Query("""
    SELECT AVG(s.score)
    FROM Submission s
    WHERE s.userId = :userId AND s.status = 'COMPLETE'
""")
    Double findAverageScore(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT e.course.id) FROM Enrollment e WHERE e.user.id = :userId")
    int countTotalCourses(@Param("userId") Long userId);



    @Query("""
    SELECT AVG(s.score)
    FROM Submission s
    WHERE s.user.id = :userId
      AND s.status = 'COMPLETE'
      AND s.submittedAt BETWEEN :fromDate AND :toDate
    """)
    Double findAverageScoreInRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );


    @Query("""
    SELECT COUNT(s)
    FROM Submission s
    WHERE s.userId = :userId AND s.status = 'COMPLETE'
""")
    Long countCompletedAssignments(@Param("userId") Long userId);

    @Query("""
    SELECT COUNT(s)
    FROM Submission s
    WHERE s.userId = :userId
      AND s.submittedAt BETWEEN :fromDate AND :toDate
""")
    Long countSubmissionsInRange(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );


    @Query("""
    SELECT DISTINCT CAST(FUNCTION('DATE', s.submittedAt) AS LocalDate)
    FROM Submission s
    WHERE s.userId = :userId
    ORDER BY CAST(FUNCTION('DATE', s.submittedAt) AS LocalDate)
""")
    List<LocalDate> findAllActiveDates(@Param("userId") Long userId);




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
        JOIN courses_assignments ca ON a.id = ca.assignments_id
        WHERE ca.courses_id = :courseId
        ORDER BY s.submitted_at DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM submissions s
        JOIN assignments a ON s.assignment_id = a.id
        JOIN courses_assignments ca ON a.id = ca.assignments_id
        WHERE ca.courses_id = :courseId
        """,
        nativeQuery = true)
    Page<Submission> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    /**
     * Lấy submissions của một assignment trong course
     */
    @Query(value = """
        SELECT s.* FROM submissions s
        JOIN assignments a ON s.assignment_id = a.id
        WHERE a.id = :assignmentId AND a.id IN (
            SELECT assignments_id FROM courses_assignments WHERE courses_id = :courseId
        )
        ORDER BY s.submitted_at DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM submissions s
        JOIN assignments a ON s.assignment_id = a.id
        WHERE a.id = :assignmentId AND a.id IN (
            SELECT assignments_id FROM courses_assignments WHERE courses_id = :courseId
        )
        """,
        nativeQuery = true)
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
     * Chỉ hiển thị thông tin cơ bản: studentId, studentName, studentEmail, score, passed, submittedAt, attemptNo
     */
    @Query("""
    SELECT 
        s.user.id,
        s.user.name,
        s.user.email,
        s.score,
        s.passed,
        s.submittedAt, 
        s.attemptNo
    FROM Submission s
    WHERE s.assignment.id = :assignmentId 
      AND s.course.id = :courseId
    ORDER BY s.submittedAt DESC
    """)
    Page<Object[]> findStudentSubmissionsByCourseAndAssignment(
            @Param("courseId") Long courseId,
            @Param("assignmentId") Long assignmentId,
            Pageable pageable
    );
    
    /**
     * Tìm submission mới nhất của một student cho một assignment
     */
    Optional<Submission> findTopByAssignmentIdAndUserIdOrderBySubmittedAtDesc(
            Long assignmentId, 
            Long userId
    );
    
    /**
     * Lấy tất cả submissions của một user, sắp xếp theo thời gian mới nhất
     */
    Page<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);

    @Query("""
    SELECT COUNT(s) 
    FROM Submission s, CourseAssignment ca
    WHERE s.assignment.id = ca.assignmentId
    AND ca.courseId = :courseId
    AND s.userId = :studentId
""")
    long countSubmittedAssignments(Long courseId, Long studentId);

    List<Submission> findByUserIdAndCourseIdAndAssignmentIdOrderByAttemptNoDesc(
            Long userId,
            Long courseId,
            Long assignmentId
    );

    @Query("""
        SELECT s FROM Submission s 
        JOIN FETCH s.assignment a 
        LEFT JOIN FETCH a.assignmentEvaluations 
        WHERE s.id = :id
    """)
    Optional<Submission> findByIdWithAssignment(@Param("id") Long id);
}
