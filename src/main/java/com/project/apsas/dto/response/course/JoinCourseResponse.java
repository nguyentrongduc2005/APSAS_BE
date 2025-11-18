package com.project.apsas.dto.response.course;

import com.project.apsas.dto.response.CourseItemStudentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinCourseResponse {
    private boolean joined;
    private CourseItemStudentResponse course;
}
