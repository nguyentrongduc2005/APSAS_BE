package com.project.apsas.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String visibility;
    private String type;
    private String avatarUrl;
    private Integer limit;
    private Long tutorialId;
    private String tutorialTitle;
    private Integer assignmentsCount;
    private Integer contentsCount;
    private LocalDateTime createdAt;
}
