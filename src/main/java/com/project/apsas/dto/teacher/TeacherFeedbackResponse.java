package com.project.apsas.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.time.LocalDateTime;

/**
 * Dùng để trả dữ liệu feedback ra cho client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherFeedbackResponse {

    Long id;
    String body;
    LocalDateTime createdAt;
    Long submissionId;
}
