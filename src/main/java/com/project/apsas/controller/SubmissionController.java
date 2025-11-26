package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.CreateSubmissionRequest;
import com.project.apsas.dto.response.CreateSubmissionResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.SubmissionResponse;
import com.project.apsas.dto.StudentSubmissionDTO;
import com.project.apsas.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API để giáo viên xem submissions của học sinh
 */
@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor

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
    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
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
    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
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
    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
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
     * GET /api/teacher/submissions/course/{courseId}/assignment/{assignmentId}/students?page=0&size=10
     *
     * @param courseId Course ID
     * @param assignmentId Assignment ID
     * @param pageable Pageable object (page, size, sort)
     * @return Paginated list of student submissions
     */
    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/course/{courseId}/assignment/{assignmentId}/students")
    public ResponseEntity<Page<StudentSubmissionDTO>> getStudentsByAssignment(
            @PathVariable Long courseId,
            @PathVariable Long assignmentId,
            Pageable pageable
    ) {
        Page<StudentSubmissionDTO> response = submissionService
                .getStudentSubmissionsByAssignment(courseId, assignmentId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả submissions của một student trong course
     * Bao gồm cả assignments chưa nộp
     * GET /api/submissions/course/{courseId}/student/{studentId}?page=0&size=10&sort=assignmentTitle,asc
     *
     * @param courseId Course ID
     * @param studentId Student ID
     * @param pageable Pageable object (page, size, sort)
     * @return Paginated list of all assignments with submission status
     */
    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/course/{courseId}/student/{studentId}")
    public ResponseEntity<Page<com.project.apsas.dto.StudentAllSubmissionsDTO>> getAllSubmissionsOfStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        Page<com.project.apsas.dto.StudentAllSubmissionsDTO> response = submissionService
                .getAllSubmissionsOfStudent(courseId, studentId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Student xem các assignment đã nộp của chính mình
     * CHỈ hiển thị assignments ĐÃ SUBMIT
     * GET /api/submissions/my-submissions?page=0&size=10&sort=submittedAt,desc
     *
     * @param pageable Pageable object (page, size, sort)
     * @return Paginated list of submitted assignments
     */
    @PreAuthorize("hasAuthority('SUBMIT_ASSIGNMENT')")
    @GetMapping("/my-submissions")
    public ResponseEntity<Page<com.project.apsas.dto.StudentSubmittedAssignmentDTO>> getMySubmittedAssignments(
            Pageable pageable
    ) {
        Page<com.project.apsas.dto.StudentSubmittedAssignmentDTO> response = submissionService
                .getMySubmittedAssignments(pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('SUBMIT_ASSIGNMENT')")
    @PostMapping("/create")
    public ApiResponse<CreateSubmissionResponse> createSubmission(
            @RequestBody CreateSubmissionRequest createSubmissionRequest) {
        return ApiResponse.<CreateSubmissionResponse>builder()
                .code("ok")
                .message("successfully")
                .data(submissionService.createSubmission(createSubmissionRequest))
                .build();
    }
}
