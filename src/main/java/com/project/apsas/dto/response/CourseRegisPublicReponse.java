package com.project.apsas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CourseRegisPublicReponse {
    Long id;
    String name;
    String description;
    String url;

    Long totalStudents;
    Long lessonsCount;
    Long totalAssignments;
    InstructorInfo instructor;
    @Data
    @Builder
    public static class InstructorInfo {
        private Long id;
        private String name; // TS. Trần Minh Quân
        private String email; // minhqnan@vnu.edu.vn
        private Long coursesCount; // 4 khóa học
        private Long studentViews; // 8.900 SV
    }

}
