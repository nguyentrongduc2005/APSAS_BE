package com.project.apsas.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherFeedbackRequest {

    @NotBlank(message = "Nội dung feedback không được để trống")
    private String body;
}
