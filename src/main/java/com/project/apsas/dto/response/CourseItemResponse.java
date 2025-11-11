package com.project.apsas.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseItemResponse {

    private Long id;
    private String name;
    private String code;
    private String visibility;
    private Integer limit;
    private String type;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
