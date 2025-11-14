package com.project.apsas.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body khi giảng viên gửi feedback cho một submission
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherFeedbackRequest {
    /**
     * Nội dung feedback của giảng viên
     */
    private String body;
}
