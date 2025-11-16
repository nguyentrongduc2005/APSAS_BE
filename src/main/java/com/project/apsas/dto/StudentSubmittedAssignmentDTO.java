package com.project.apsas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO để student xem danh sách bài tập đã nộp của chính mình
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubmittedAssignmentDTO {
    
    // Assignment info
    private Long assignmentId;
    private String assignmentTitle;
    private String assignmentDescription;
    private Integer assignmentMaxScore;
    
    // Course info
    private Long courseId;
    private String courseName;
    
    // Submission info
    private Long submissionId;
    private String status; // PENDING, PROCESSING, COMPLETE, FAILED
    private Double score;
    private Boolean passed;
    private LocalDateTime submittedAt;
    private Integer attemptNo;
    private String language;
    private String feedback;
    
    // Additional info
    private Boolean isLatest; // true nếu đây là submission mới nhất
}
