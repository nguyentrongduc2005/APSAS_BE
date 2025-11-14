package com.project.apsas.controller;

import com.project.apsas.dto.teacher.TeacherFeedbackRequest;
import com.project.apsas.dto.teacher.TeacherFeedbackResponse;
import com.project.apsas.service.TeacherFeedbackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API cho giảng viên gửi feedback text cho submission của sinh viên
 */
@RestController
@RequestMapping("/api/teacher/submissions/{submissionId}/feedback")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherFeedbackController {

    TeacherFeedbackService teacherFeedbackService;

    /**
     * Giảng viên tạo feedback cho một submission
     */
    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<TeacherFeedbackResponse> createFeedback(
            @PathVariable Long submissionId,
            @RequestBody TeacherFeedbackRequest request
    ) {
        TeacherFeedbackResponse response = teacherFeedbackService.addFeedback(submissionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách feedback của một submission
     */
    @GetMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<TeacherFeedbackResponse>> getFeedbacks(
            @PathVariable Long submissionId
    ) {
        List<TeacherFeedbackResponse> feedbacks =
                teacherFeedbackService.getFeedbacksBySubmission(submissionId);
        return ResponseEntity.ok(feedbacks);
    }
}
