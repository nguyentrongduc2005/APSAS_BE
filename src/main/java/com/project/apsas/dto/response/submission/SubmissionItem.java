package com.project.apsas.dto.response.submission;

import com.project.apsas.enums.StatusSubmission;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionItem {
    private Long id;
    private boolean passed;
    private StatusSubmission status;
    private BigDecimal score;
    private Integer attemptNo;
    private String language;
    private LocalDateTime submittedAt;
}
