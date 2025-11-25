package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.student.StudentCourseInsightDto;
import com.project.apsas.service.StudentInsightService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/insight")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentInsightController {

    StudentInsightService studentInsightService;

    @GetMapping("/my-courses")
    @PreAuthorize("hasAuthority('VIEW_COURSES') or hasAuthority('DASHBOARD_VIEW')")
    public ApiResponse<List<StudentCourseInsightDto>> getMyCourseInsights() {
        List<StudentCourseInsightDto> data = studentInsightService.getMyCourseInsights();

        return ApiResponse.<List<StudentCourseInsightDto>>builder()
                .code("200")
                .message("Lấy insight khóa học của sinh viên thành công")
                .data(data)
                .build();
    }
}
