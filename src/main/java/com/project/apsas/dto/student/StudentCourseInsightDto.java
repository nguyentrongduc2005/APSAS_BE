package com.project.apsas.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourseInsightDto {

    private Long courseId;
    private String courseCode;
    private String courseName;

    private Long totalAssignments;   // số assignment đã từng nộp
    private Long totalSubmissions;   // tổng số lượt nộp
    private Double avgScore;         // điểm trung bình
}
