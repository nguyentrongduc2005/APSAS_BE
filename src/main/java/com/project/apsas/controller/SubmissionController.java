package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.CreateSubmissionRequest;
import com.project.apsas.dto.response.CreateSubmissionResponse;
import com.project.apsas.dto.response.submission.StudentSubmissionDTO;
import com.project.apsas.dto.response.submission.SubmissionDetailResponse;
import com.project.apsas.dto.response.submission.SubmittedAssigmentResponse;
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


    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/course/{courseId}/assignment/{assignmentId}/students")
    public ResponseEntity<ApiResponse <Page<StudentSubmissionDTO>>> getStudentsByAssignment(
            @PathVariable Long courseId,
            @PathVariable Long assignmentId,
            Pageable pageable
    ) {
         Page<StudentSubmissionDTO> response = submissionService
                .getStudentSubmissionsByAssignment(courseId, assignmentId, pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<StudentSubmissionDTO>>builder()
                        .code("OK")
                        .message("Lấy danh sách bài nộp thành công")
                        .data(response)
                        .build()
        );
    }

//    get submission by id user, assigment id, course id


//    get submission vừa nộp by assigmnet id , course id


//    get submission detail by id

    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/history")
    public ApiResponse<SubmittedAssigmentResponse> getSubmissionHistory(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long studentId
            // @AuthenticationPrincipal UserDetails userDetails // Nếu dùng Spring Security để lấy User hiện tại
    ) {
        // Tạm thời fix cứng userId để test, thực tế bạn sẽ lấy từ Security Context
        ;

        SubmittedAssigmentResponse response = submissionService.getSubmissionHistory( courseId, assignmentId, studentId);

        return ApiResponse.<SubmittedAssigmentResponse>builder()
                .code("ok")
                .message("success")
                .data(response)
                .build();
    }

    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/{id}")
    public ApiResponse<SubmissionDetailResponse> getSubmissionDetail(@PathVariable Long id) {
        // Có thể thêm logic kiểm tra xem User hiện tại có phải chủ nhân bài nộp không ở đây
        SubmissionDetailResponse response = submissionService.getSubmissionDetailForStudent(id);
        return ApiResponse.<SubmissionDetailResponse>builder()
                .code("ok")
                .message("success")
                .data(response)
                .build();
    }

    @PreAuthorize("hasAuthority('VIEW_SUBMISSIONS')")
    @GetMapping("/teacher/submissions/{id}")
    public ApiResponse<SubmissionDetailResponse> getSubmissionDetailTeacher(@PathVariable Long id) {
        // Có thể thêm logic kiểm tra xem User hiện tại có phải chủ nhân bài nộp không ở đây
        SubmissionDetailResponse response = submissionService.getSubmissionDetailForTeacher(id);
        return ApiResponse.<SubmissionDetailResponse>builder()
                .code("ok")
                .message("success")
                .data(response)
                .build();
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
