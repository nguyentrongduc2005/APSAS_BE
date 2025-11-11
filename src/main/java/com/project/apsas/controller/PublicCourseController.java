package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCourseController {

    private final PublicCourseService publicCourseService;

    @GetMapping("/courses")
    public ApiResponse<PagedResponse<PublicCourseItem>> getPublicCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String search
    ) {
        var data = publicCourseService.getPublicCourses(page, limit, search);
        return ApiResponse.<PagedResponse<PublicCourseItem>>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }
}
