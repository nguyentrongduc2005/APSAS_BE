package com.project.apsas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String assignmentTitle;
}
