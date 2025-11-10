package com.project.apsas.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentCourseCardDto {
    private Long id;
    private String title;                 // map từ Course.name
    private String visibility;            // PUBLIC | PRIVATE

    // KPI của khóa (tổng quan – cho card)
    private Integer totalAssignments;

    // KPI của CHÍNH SINH VIÊN trong khóa
    private Integer mySubmissions;
    private Integer myGradedSubmissions;
    private Integer myPendingSubmissions;
    private Double  myAverageScore;       // làm tròn ở service
    private Double  myCompletionRate;     // = myGradedSubmissions / totalAssignments
}
