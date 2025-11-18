package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.TeacherFeedbackCreateRequest;
import com.project.apsas.dto.response.TeacherFeedbackDTO;
import com.project.apsas.service.TeacherFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API giảng viên cho feedback cho submission của sinh viên
 *
 * Full path: /api/submissions/{submissionId}/feedbacks
 * (vì context-path = /api trong application.yml)
 */
@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class TeacherFeedbackController {

    private final TeacherFeedbackService teacherFeedbackService;

    /**
     * Giảng viên tạo feedback cho submission
     */
    @PreAuthorize("hasAuthority('EVALUATE_SUBMISSIONS')")
    @PostMapping("/{submissionId}/feedbacks")
    public ApiResponse<TeacherFeedbackDTO> createTeacherFeedback(
            @PathVariable Long submissionId,
            @RequestBody TeacherFeedbackCreateRequest request
    ) {
        TeacherFeedbackDTO data =
                teacherFeedbackService.createTeacherFeedback(submissionId, request);

        return ApiResponse.<TeacherFeedbackDTO>builder()
                .code("ok")
                .message("FEEDBACK_CREATED")
                .data(data)
                .build();
    }

    /**
     * Lấy danh sách feedback cho 1 submission
     */

}
