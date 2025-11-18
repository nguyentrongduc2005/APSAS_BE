package com.project.apsas.service;

import com.project.apsas.dto.request.TeacherFeedbackCreateRequest;
import com.project.apsas.dto.response.TeacherFeedbackDTO;

import java.util.List;

public interface TeacherFeedbackService {

    /**
     * Giảng viên tạo feedback cho một submission
     */
    TeacherFeedbackDTO createTeacherFeedback(Long submissionId,
                                             TeacherFeedbackCreateRequest request);

    /**
     * Lấy danh sách feedback của 1 submission
     */

}
