package com.project.apsas.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CourseDetailResponse {
    private Long id;
    private String name;
    private String code;
    private String visibility;    // PUBLIC/PRIVATE/UNLISTED
    private LocalDateTime createdAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Instructor {
        private Long id;
        private String name;
        private String email;
        private String avatar;    // có thể null nếu schema chưa có
    }
    private Instructor instructor;

    private Long studentsCount;
    private Long lessonsCount;

    // Optional: vài lesson đầu để hiển thị preview (nếu cần)
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Lesson {
        private Long id;
        private String title;
        private Integer orderIndex;
    }
    private List<Lesson> topLessons; // max 5
}
