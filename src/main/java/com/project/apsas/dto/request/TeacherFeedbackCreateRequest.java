package com.project.apsas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherFeedbackCreateRequest {
    /**
     * Nội dung feedback của giảng viên cho submission
     */
    private String body;
}
