package com.project.apsas.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentCourseCardDto {
    private Long id;
    private String title;
    private String visibility;            

    private Integer totalAssignments;
    private Integer mySubmissions;
    private Integer myGradedSubmissions;
    private Integer myPendingSubmissions;

    private Double myAverageScore;
    private Double myCompletionRate;
}
