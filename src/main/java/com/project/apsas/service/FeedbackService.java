package com.project.apsas.service;

import com.project.apsas.dto.request.TeacherFeedbackRequest;
import com.project.apsas.dto.response.FeedbackResponse;

import java.util.List;

public interface FeedbackService {

    /**
     * Giảng viên tạo feedback cho 1 submission cụ thể
     */
    FeedbackResponse addTeacherFeedback(Long submissionId, TeacherFeedbackRequest request);

    /**
     * Lấy danh sách feedback (cả giảng viên lẫn hệ thống, nếu sau này mở rộng)
     * cho 1 submission
     */
    List<FeedbackResponse> getFeedbacksOfSubmission(Long submissionId);
}
