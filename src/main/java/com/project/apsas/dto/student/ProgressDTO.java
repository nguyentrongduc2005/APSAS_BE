package com.project.apsas.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgressDTO {

    private String name;
    private String email;
    private long totalCourses;
    private int completedCourses;
    private double averageScore;
    private List<DailyScoreDTO> dailyScoreDTOList;


}