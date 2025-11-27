package com.project.apsas.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicCourseItem {
    private Long id;
    private String name;
    private String url;
    private String description;
    private Long studentsCount;
    private Long lessonsCount;
    private Long lessonsCountTotal;
}
