package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.CreateCourseFromTutorialRequest;
import com.project.apsas.dto.request.course.JoinCourseRequest;
import com.project.apsas.dto.response.*;
import com.project.apsas.dto.response.course.JoinCourseResponse;
import com.project.apsas.dto.teacher.CreateCourseRequestDTO;
import com.project.apsas.dto.teacher.CreateCourseResponseDTO;
import com.project.apsas.service.CourseServices;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    @GetMapping("/student/my-courses")
    @PreAuthorize("hasAuthority('VIEW_COURSES')")
    public ApiResponse<Page<CourseItemStudentResponse>> getMyCoursesStudent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc", required = false) String[] sort,
            @RequestParam(required = false) String search
    ) {
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<CourseItemStudentResponse> data = service.getMyCoursesStudent(pageable, search);
        return ApiResponse.<Page<CourseItemStudentResponse>>builder()
                .code("ok")
                .message("SUCCESS")
                .data(data)
                .build();
    }

    @GetMapping("/lecture/my-courses")
    @PreAuthorize("hasAuthority('CREATE_COURSE')")
    public ApiResponse<Page<CourseItemTeacherResponse>> getMyCoursesLecture(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc", required = false) String[] sort,
            @RequestParam(required = false) String search
    ) {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().toList().toString());
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<CourseItemTeacherResponse> data = service.getMyCoursesTeacher(pageable, search);
        return ApiResponse.<Page<CourseItemTeacherResponse>>builder()
                .code("ok")
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

    @GetMapping("/{courseId}/teacher-overview")
    @PreAuthorize("hasAuthority('VIEW_COURSES')")
    public ApiResponse<CourseRegisResponse> getCourseDetailForTeacher(@PathVariable Long courseId) {
        var data = service.getCourseRegistrationDetails(courseId);
        return ApiResponse.<CourseRegisResponse>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }
    @PreAuthorize("hasAuthority('VIEW_COURSES')")
    @GetMapping("/{id}/detail-student")
    public ApiResponse<DetailCourseStudentResponse> getCourseDetail(@PathVariable Long id) {
        return ApiResponse.<DetailCourseStudentResponse>builder()
                .code("ok")
                .message("succces")
                .data(service.getCourseDetailForStudent(id))
                .build();
    }
    @GetMapping("/{courseId}/register-details")
    public ApiResponse<CourseRegisPublicReponse> getCourseDetailregis(@PathVariable Long courseId) {
        var data = service.getCourseRegistrationDetailss(courseId);
        return ApiResponse.<CourseRegisPublicReponse>builder()
                .code("0")
                .message("SUCCESS")
                .data(data)
                .build();
    }

    @PostMapping("/from-tutorial")
    @PreAuthorize("hasAuthority('CREATE_COURSE')")
    public ApiResponse<CreateCourseResponse> createCourseFromTutorial(
            @RequestBody @Valid CreateCourseFromTutorialRequest request) {
        
        var data = service.createCourseFromTutorial(request);
        
        return ApiResponse.<CreateCourseResponse>builder()
                .code("0")
                .message("Tạo khóa học từ tutorial thành công")
                .data(data)
                .build();
    }
    @PreAuthorize("hasAuthority('ENROLL_COURSE')")
    @PostMapping("/join")
    public ApiResponse<JoinCourseResponse> joinCourse(@RequestBody JoinCourseRequest request) {
        return ApiResponse.<JoinCourseResponse>builder()
                .code("ok")
                .message("SUCCESS")
                .data(service.joinCourse(request))
                .build();
    }

    @PreAuthorize("hasAuthority('CREATE_COURSE')")
    @PostMapping("/create")
    public ApiResponse<CreateCourseResponseDTO> createCourse(
            @Valid @RequestBody CreateCourseRequestDTO request) {

        CreateCourseResponseDTO response = service.createCourse(request);

        return ApiResponse.<CreateCourseResponseDTO>builder()
                .code("ok")
                .message("COURSE_CREATED_SUCCESS")
                .data(response)
                .build();
    }

    @PostMapping("/{courseId}/avatar")
    @PreAuthorize("hasAuthority('CREATE_COURSE')")
    public ApiResponse<CourseAvatarResponseDTO> uploadCourseAvatar(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file) throws IOException {

        CourseAvatarResponseDTO response = service.updateCourseAvatar(courseId, file);

        return ApiResponse.<CourseAvatarResponseDTO>builder()
                .code("ok")
                .message("AVATAR_UPLOADED_SUCCESS")
                .data(response)
                .build();
    }
}
