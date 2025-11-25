package com.project.apsas.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherCourseInsightDto {

    private Long courseId;
    private String courseCode;
    private String courseName;

    private Long totalStudents;      // số SV khác nhau đã nộp
    private Long totalAssignments;   // số assignment có ít nhất một submission
    private Long totalSubmissions;   // số lượt nộp
    private Double avgScore;         // điểm trung bình của course
}
