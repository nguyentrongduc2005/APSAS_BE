package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;

import com.project.apsas.dto.response.CourseItemTeacherResponse;
import com.project.apsas.dto.response.CourseRegisResponse;

import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.service.CourseServices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseServices service;


    //    @PostMapping
//    public ApiResponse<CourseItemResponse> createCourse(@RequestBody @Valid CreateCourseRequest req) {
//        var data = service.create(req);
//        return ApiResponse.<CourseItemResponse>builder()
//                .code("0")
//                .message("SUCCESS")
//                .data(data)
//                .build();
//    }
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Page<CourseItemTeacherResponse>> getMyCoursesStudent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc", required = false) String[] sort,
            @RequestParam(required = false) String search
    ) {
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<CourseItemTeacherResponse> data = service.getMyCoursesTeacher(pageable, search);
        return ApiResponse.<Page<CourseItemTeacherResponse>>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }


    @GetMapping()
    public ApiResponse<Page<PublicCourseItem>> getPublicCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc", required = false) String[] sort,
            @RequestParam(required = false) String search
    ) {
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        var data = service.getPublicCourses(pageable, search);
        return ApiResponse.<Page<PublicCourseItem>>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }

    private Sort createSortObject(String[] sort) {
        Sort sortList = Sort.unsorted();
        for (String s : sort) {
            String[] parts = s.split(",");
            if (parts.length == 2) {
                String property = parts[0].trim();
                Sort.Direction direction = "desc".equalsIgnoreCase(parts[1].trim()) ? Sort.Direction.DESC : Sort.Direction.ASC;
                sortList = sortList.and(Sort.by(direction, property));
            }
        }
        return sortList;
    }

    @GetMapping("/{courseId}/register-details")
    public ApiResponse<CourseRegisResponse> getRegistrationDetails(@PathVariable Long courseId) {

        // Gọi Service để lấy dữ liệu chi tiết
        var data = service.getCourseRegistrationDetails(courseId);

        // Trả về phản hồi chuẩn
        return ApiResponse.<CourseRegisResponse>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }


}
