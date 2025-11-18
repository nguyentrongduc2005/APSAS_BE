package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.CourseDetailResponse;
import com.project.apsas.service.CourseDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class CourseDetailController {

    private final CourseDetailService courseDetailService;

    @GetMapping("/courses/{id}")
    public ApiResponse<CourseDetailResponse> getCourseDetail(@PathVariable Long id) {
        CourseDetailResponse data = courseDetailService.getPublicDetail(id);
        return ApiResponse.<CourseDetailResponse>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }
}
