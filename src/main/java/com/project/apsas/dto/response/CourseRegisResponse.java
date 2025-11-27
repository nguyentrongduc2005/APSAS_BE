package com.project.apsas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CourseRegisResponse {
    Long id;
    String name;
    String description;
    Long totalStudents;
    InstructorInfo instructor;
    List<ContentItem> contents;
    List<AssignmentItem> assignments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorInfo {
        private Long id;
        private String name;
        private String email;
        private Long coursesCount;
        private Long studentViews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItem {
        private Long id;
        private String title;
        private Integer orderNo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentItem {
        private Long id;
        private String title;
        private LocalDateTime openAt;
        private LocalDateTime dueAt;
    }
}
