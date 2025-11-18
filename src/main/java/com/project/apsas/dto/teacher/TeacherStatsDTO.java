package com.project.apsas.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherStatsDTO {
    private int totalStudents;
    private int totalCourses;
    private int totalLessons;
}
