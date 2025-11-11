package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.CreateCourseRequest;
import com.project.apsas.dto.response.CourseItemResponse;
import com.project.apsas.service.CourseServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseServices service;

    @PostMapping
    public ApiResponse<CourseItemResponse> createCourse(@RequestBody @Valid CreateCourseRequest req) {
        var data = service.create(req);
        return ApiResponse.<CourseItemResponse>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }
}
