package com.project.apsas.controller;

import com.project.apsas.dto.response.CourseDetailResponse;
import com.project.apsas.service.CourseDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class CourseDetailController {

    private final CourseDetailService service;

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDetailResponse> getCourseDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getPublicDetail(id));
    }
}
