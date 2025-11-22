package com.project.apsas.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequestDTO {

    @NotBlank(message = "Course name is required")
    @Size(max = 160, message = "Course name must not exceed 160 characters")
    private String name;

    private String description;

    @Size(max = 60, message = "Course code must not exceed 60 characters")
    private String code;

    private String visibility; // PUBLIC, PRIVATE, UNLISTED

    private String type;

    private String avatarUrl;

    private Integer limit;



    @NotNull(message = "Tutorial ID is required")
    private Long tutorialId;

    private List<Long> selectedContentIds;
    private List<AssignmentScheduleDTO> assignmentSchedules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentScheduleDTO {
        @NotNull(message = "Assignment ID is required")
        private Long assignmentId;

        private String openAt; // ISO format: 2024-01-15T10:00:00
        private String dueAt;  // ISO format: 2024-01-20T23:59:59
    }
}