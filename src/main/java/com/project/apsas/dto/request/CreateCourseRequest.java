package com.project.apsas.dto.request;

import com.project.apsas.enums.CourseVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description; // optional

    @NotNull(message = "Visibility is required")
    private CourseVisibility visibility; // PUBLIC / PRIVATE (theo enum của bạn)
}
