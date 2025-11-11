package com.project.apsas.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicCourseItem {
    private Long id;
    private String name;
    private String code;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Instructor {
        private Long id;
        private String name;
    }
    private Instructor instructor;

    private Long studentsCount;
    private Long lessonsCount;

    private LocalDateTime createdAt;
    private String visibility;
}
