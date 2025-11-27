package com.project.apsas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailCourseStudentResponse {
    private String name;
    private String description;
    private int totalStudent;
    private int totalLession;
    private int totalAssignment;
    private int progressAverage;
    private List<ContentItem> contentItems;
    private List<AssignmentItem> assignments;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItem {
        private Long id;
        private String title;
        private Integer orderNo;
        private int totalMedia;
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
        private Integer orderNo;
    }
}
