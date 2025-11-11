package com.project.apsas.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for displaying student submissions to teacher
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionResponse {

    // Submission info
    Long id;
    Long assignmentId;
    String assignmentTitle;

    // Student info
    Long studentId;
    String studentName;
    String studentEmail;

    // Code & Results
    String language;
    BigDecimal score;
    Boolean passed;
    String feedback;
    Integer attemptNo;
    LocalDateTime submittedAt;

    // Additional metadata
    String codePreview;  // First 500 chars of code
    Integer codeLength;
    String reportJson;
}
