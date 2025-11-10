package com.project.apsas.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentCourseDetailsDto {
    private Long id;
    private String title;                 // Course.name
    private String code;
    private String visibility;
    private Integer limit;
    private LocalDateTime createdAt;

    // KPI của khóa
    private Integer totalAssignments;

    // KPI của CHÍNH SINH VIÊN trong khóa
    private Integer mySubmissions;
    private Integer myGradedSubmissions;
    private Integer myPendingSubmissions;
    private Double  myAverageScore;
    private Double  myCompletionRate;     // = myGradedSubmissions / totalAssignments
}
