package com.project.apsas.service;

import com.project.apsas.dto.request.CreateCourseFromTutorialRequest;
import com.project.apsas.dto.request.course.JoinCourseRequest;
import com.project.apsas.dto.response.CourseItemStudentResponse;
import com.project.apsas.dto.response.CourseItemTeacherResponse;
import com.project.apsas.dto.response.CourseRegisResponse;
import com.project.apsas.dto.response.CreateCourseResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.dto.response.course.CourseResourceListDTO;
import com.project.apsas.dto.response.course.JoinCourseResponse;
import com.project.apsas.dto.teacher.CreateCourseRequestDTO;
import com.project.apsas.dto.teacher.CreateCourseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseServices {
    Page<PublicCourseItem> getPublicCourses(Pageable pageable, String search);
    CourseRegisResponse getCourseRegistrationDetails(Long courseId);
    Page<CourseItemTeacherResponse> getMyCoursesTeacher(Pageable pageable, String search);
    Page<CourseItemStudentResponse> getMyCoursesStudent(Pageable pageable, String search);
    CreateCourseResponse createCourseFromTutorial(CreateCourseFromTutorialRequest request);
    JoinCourseResponse joinCourse(JoinCourseRequest request);
    CourseResourceListDTO getAvailableResources();
    CreateCourseResponseDTO createCourse(CreateCourseRequestDTO request);
}
