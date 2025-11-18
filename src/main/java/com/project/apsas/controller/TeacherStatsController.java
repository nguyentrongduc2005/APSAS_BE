package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.teacher.TeacherStatsDTO;
import com.project.apsas.service.TeacherStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherStatsController {
    private final TeacherStatsService teacherStatsService;
    @PreAuthorize("hasAuthority('VIEW_TEACHER_STATS')")
    @GetMapping("/stats/total-students")
    public ApiResponse<TeacherStatsDTO> getTotalStudents() {
        TeacherStatsDTO stats = teacherStatsService.getTeacherStats();

        return ApiResponse.<TeacherStatsDTO>builder()
                .code("200")
                .message("Lâý ra học sinh thành công")
                .data(stats)
                .build();
    }
}
