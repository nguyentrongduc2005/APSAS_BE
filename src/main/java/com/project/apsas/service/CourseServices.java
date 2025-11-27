package com.project.apsas.service;

import com.project.apsas.dto.request.CreateCourseFromTutorialRequest;
import com.project.apsas.dto.request.course.JoinCourseRequest;
import com.project.apsas.dto.response.*;
import com.project.apsas.dto.response.course.JoinCourseResponse;
import com.project.apsas.dto.teacher.CreateCourseRequestDTO;
import com.project.apsas.dto.teacher.CreateCourseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CourseServices {
    Page<PublicCourseItem> getPublicCourses(Pageable pageable, String search);
    CourseRegisResponse getCourseRegistrationDetails(Long courseId);
    DetailCourseStudentResponse getCourseDetailForStudent(Long courseId);
    Page<CourseItemTeacherResponse> getMyCoursesTeacher(Pageable pageable, String search);
    Page<CourseItemStudentResponse> getMyCoursesStudent(Pageable pageable, String search);
    CreateCourseResponse createCourseFromTutorial(CreateCourseFromTutorialRequest request);
    JoinCourseResponse joinCourse(JoinCourseRequest request);
    CreateCourseResponseDTO createCourse(CreateCourseRequestDTO request);
    CourseAvatarResponseDTO updateCourseAvatar(Long courseId, MultipartFile file) throws IOException;
    public CourseRegisPublicReponse getCourseRegistrationDetailss(Long courseId);
}
