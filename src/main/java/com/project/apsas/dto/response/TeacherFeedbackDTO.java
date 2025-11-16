package com.project.apsas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherFeedbackDTO {
    private Long id;
    private String body;
    private LocalDateTime createdAt;
}
