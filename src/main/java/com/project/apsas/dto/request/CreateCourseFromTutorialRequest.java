package com.project.apsas.dto.request;

import com.project.apsas.enums.CourseVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseFromTutorialRequest {

    @NotNull(message = "Tutorial ID is required")
    @Positive(message = "Tutorial ID must be positive")
    private Long tutorialId;

    @NotBlank(message = "Course name is required")
    private String name;

    private String code; // optional, có thể auto-generate

    private String description; // optional

    @NotNull(message = "Visibility is required")
    private CourseVisibility visibility; // PUBLIC, PRIVATE, UNLISTED

    private String type; // optional: PROGRAMMING, FRAMEWORK, etc.

    private String avatarUrl; // optional

    private Integer limit; // optional: giới hạn số học viên
}
