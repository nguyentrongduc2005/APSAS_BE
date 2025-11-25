package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.teacher.TeacherCourseInsightDto;
import com.project.apsas.service.TeacherInsightService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teacher/insight")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherInsightController {

    TeacherInsightService teacherInsightService;

    @GetMapping("/my-courses")
    @PreAuthorize("hasAuthority('VIEW_COURSES') or hasAuthority('VIEW_TEACHER_STATS')")
    public ApiResponse<List<TeacherCourseInsightDto>> getMyCourseInsights() {
        List<TeacherCourseInsightDto> data = teacherInsightService.getMyCourseInsights();

        return ApiResponse.<List<TeacherCourseInsightDto>>builder()
                .code("200")
                .message("Lấy insight khóa học của giảng viên thành công")
                .data(data)
                .build();
    }
}
