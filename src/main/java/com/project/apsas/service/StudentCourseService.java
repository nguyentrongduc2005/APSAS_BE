package com.project.apsas.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.apsas.dto.response.StudentCourseCardDto;
import com.project.apsas.dto.response.StudentCourseDetailsDto;
import com.project.apsas.enums.CourseVisibility;

public interface StudentCourseService {

    Page<StudentCourseCardDto> myCourses(Long studentId,
                                         String keyword,
                                         CourseVisibility visibility,
                                         Pageable pageable);

    StudentCourseDetailsDto getCourseDetails(Long studentId, Long courseId);
}
