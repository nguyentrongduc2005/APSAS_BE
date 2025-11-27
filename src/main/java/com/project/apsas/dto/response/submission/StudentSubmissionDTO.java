package com.project.apsas.dto.response.submission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for listing students who submitted assignments
 * Contains only basic information for display in lists
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubmissionDTO {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private BigDecimal score;
    private Boolean passed;
    private LocalDateTime submittedAt;
    private Integer attemptNo;
}
