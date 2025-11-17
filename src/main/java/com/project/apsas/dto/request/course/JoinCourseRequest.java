package com.project.apsas.dto.request.course;

import lombok.Data;

@Data
public class JoinCourseRequest {
    private Long courseId; // Dùng cho khóa Public
    private String code;
}
