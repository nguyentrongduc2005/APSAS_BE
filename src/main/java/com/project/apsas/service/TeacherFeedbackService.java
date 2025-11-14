package com.project.apsas.service;

import com.project.apsas.dto.teacher.TeacherFeedbackRequest;
import com.project.apsas.dto.teacher.TeacherFeedbackResponse;

import java.util.List;

public interface TeacherFeedbackService {

    /**
     * Giảng viên tạo feedback cho một submission
     */
    TeacherFeedbackResponse addFeedback(Long submissionId, TeacherFeedbackRequest request);

    /**
     * Lấy tất cả feedback của một submission (để hiển thị trong UI giảng viên)
     */
    List<TeacherFeedbackResponse> getFeedbacksBySubmission(Long submissionId);
}
