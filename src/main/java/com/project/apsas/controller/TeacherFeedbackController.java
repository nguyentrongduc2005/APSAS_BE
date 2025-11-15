package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.TeacherFeedbackRequest;
import com.project.apsas.dto.response.FeedbackResponse;
import com.project.apsas.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/submissions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
public class TeacherFeedbackController {

    private final FeedbackService feedbackService;

    /**
     * API: Giảng viên tạo feedback cho một submission
     * POST /api/teacher/submissions/{submissionId}/feedbacks
     */
    @PostMapping("/{submissionId}/feedbacks")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createTeacherFeedback(
            @PathVariable Long submissionId,
            @RequestBody TeacherFeedbackRequest request
    ) {
        FeedbackResponse resp = feedbackService.addTeacherFeedback(submissionId, request);

        return ResponseEntity.ok(
                ApiResponse.<FeedbackResponse>builder()
                        .code("OK")
                        .message("Feedback created successfully")
                        .data(resp)
                        .build()
        );
    }

    /**
     * API: Lấy danh sách feedback của một submission
     * GET /api/teacher/submissions/{submissionId}/feedbacks
     */
    @GetMapping("/{submissionId}/feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbacks(
            @PathVariable Long submissionId
    ) {
        List<FeedbackResponse> resp = feedbackService.getFeedbacksOfSubmission(submissionId);

        return ResponseEntity.ok(
                ApiResponse.<List<FeedbackResponse>>builder()
                        .code("OK")
                        .message("List feedbacks")
                        .data(resp)
                        .build()
        );
    }
}
