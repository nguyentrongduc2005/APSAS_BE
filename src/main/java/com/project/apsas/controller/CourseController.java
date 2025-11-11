package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.CreateCourseRequest;
import com.project.apsas.dto.response.CourseItemResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.service.CourseServices;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
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


    @GetMapping("/courses")
    public ApiResponse<PagedResponse<PublicCourseItem>> getPublicCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String search
    ) {
        var data = service.getPublicCourses(page, limit, search);
        return ApiResponse.<PagedResponse<PublicCourseItem>>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }
}
