package com.project.apsas.controller;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.service.PublicCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCourseController {

    private final PublicCourseService service;

    @GetMapping("/courses")
    public ResponseEntity<PagedResponse<PublicCourseItem>> getPublicCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(service.list(page, limit, search));
    }
}
