package com.project.apsas.controller;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.SubmissionResponse;
import com.project.apsas.dto.StudentSubmissionDTO;
import com.project.apsas.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API để giáo viên xem submissions của học sinh
 */
@RestController
@RequestMapping("/api/teacher/submissions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * Lấy tất cả submissions trong một course
     * GET /api/teacher/submissions/course/{courseId}?page=1&limit=10
     *
     * @param courseId Course ID
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 10, max: 100)
     * @return Paginated list of submissions
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<PagedResponse<SubmissionResponse>> getSubmissionsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PagedResponse<SubmissionResponse> response = submissionService
                .getSubmissionsByCourse(courseId, page, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy submissions của một assignment trong course
     * GET /api/teacher/submissions/course/{courseId}/assignment/{assignmentId}?page=1&limit=10
     *
     * @param courseId Course ID
     * @param assignmentId Assignment ID
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 10, max: 100)
     * @return Paginated list of submissions
     */
    @GetMapping("/course/{courseId}/assignment/{assignmentId}")
    public ResponseEntity<PagedResponse<SubmissionResponse>> getSubmissionsByAssignment(
            @PathVariable Long courseId,
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PagedResponse<SubmissionResponse> response = submissionService
                .getSubmissionsByAssignment(courseId, assignmentId, page, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết submission của một student cho một assignment
     * GET /api/teacher/submissions/assignment/{assignmentId}/student/{studentId}
     *
     * @param assignmentId Assignment ID
     * @param studentId Student ID
     * @return Submission details
     */
    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<SubmissionResponse> getSubmissionDetail(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId
    ) {
        SubmissionResponse response = submissionService
                .getSubmissionDetail(assignmentId, studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách sinh viên đã nộp bài trong một assignment của course
     * GET /api/teacher/submissions/course/{courseId}/assignment/{assignmentId}/students?page=1&limit=10
     *
     * @param courseId Course ID
     * @param assignmentId Assignment ID
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 10, max: 100)
     * @return Paginated list of student submissions
     */
    @GetMapping("/course/{courseId}/assignment/{assignmentId}/students")
    public ResponseEntity<PagedResponse<StudentSubmissionDTO>> getStudentsByAssignment(
            @PathVariable Long courseId,
            @PathVariable Long assignmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PagedResponse<StudentSubmissionDTO> response = submissionService
                .getStudentSubmissionsByAssignment(courseId, assignmentId, page, limit);
        return ResponseEntity.ok(response);
    }
}
