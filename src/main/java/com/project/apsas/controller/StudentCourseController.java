//package com.project.apsas.controller;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.project.apsas.dto.student.StudentCourseCardDto;
//import com.project.apsas.dto.student.StudentCourseDetailsDto;
//import com.project.apsas.enums.CourseVisibility;
//import com.project.apsas.service.AuthUserResolver;
//import com.project.apsas.service.StudentCourseService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/student/courses")
//@RequiredArgsConstructor
//public class StudentCourseController {
//
//    private final StudentCourseService studentCourseService;
//    private final AuthUserResolver authUserResolver;
//
//    @PreAuthorize("hasRole('STUDENT')")
//    @GetMapping("/me")
//    public Page<StudentCourseCardDto> myCourses(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int size,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) CourseVisibility visibility,
//            @RequestParam(required = false) String sort
//    ) {
//        Long studentId = authUserResolver.currentUserId();
//        Pageable pageable = buildPageable(page, size, sort);
//        return studentCourseService.myCourses(studentId, keyword, visibility, pageable);
//    }
//
//    @PreAuthorize("hasRole('STUDENT')")
//    @GetMapping("/{id}")
//    public StudentCourseDetailsDto details(@PathVariable("id") Long courseId) {
//        Long studentId = authUserResolver.currentUserId();
//        return studentCourseService.getCourseDetails(studentId, courseId);
//    }
//
//    private Pageable buildPageable(int page, int size, String sort) {
//        Sort s;
//        if (sort == null || sort.isBlank()) {
//            s = Sort.by(Sort.Direction.DESC, "createdAt");
//        } else {
//            String[] p = sort.split(",", 2);
//            String field = p[0].trim();
//            String dir = (p.length > 1) ? p[1].trim() : "asc";
//            if (field.equals("name")) field = "name";
//            Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
//            s = Sort.by(d, field);
//        }
//        return PageRequest.of(Math.max(0, page), Math.max(1, size), s);
//    }
//}
