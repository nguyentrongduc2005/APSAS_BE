package com.project.apsas.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseAvatarResponseDTO {
    private Long courseId;
    private String avatarUrl;
    private boolean success;
    private String message;
}
