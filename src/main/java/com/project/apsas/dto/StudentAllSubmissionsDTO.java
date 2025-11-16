package com.project.apsas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO để trả về thông tin submissions của một student trong course
 * Dùng cho teacher xem tất cả bài nộp của một sinh viên cụ thể
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAllSubmissionsDTO {
    
    // Assignment info
    private Long assignmentId;
    private String assignmentTitle;
    private String assignmentDescription; // statementMd
    private Integer assignmentMaxScore;
    private String language;
    
    // Submission info (null nếu chưa nộp)
    private Long submissionId;
    private String status; // PENDING, PROCESSING, COMPLETE, FAILED, hoặc null nếu chưa nộp
    private Double score;
    private LocalDateTime submittedAt;
    private String feedback;
    private Integer attemptNo;
    private Boolean passed;
    
    // Derived fields
    private Boolean hasSubmitted; // true nếu đã nộp, false nếu chưa
}
