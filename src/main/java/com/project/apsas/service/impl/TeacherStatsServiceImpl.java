package com.project.apsas.service.impl;

import com.project.apsas.dto.teacher.TeacherStatsDTO;
import com.project.apsas.entity.Enrollment;
import com.project.apsas.enums.EnrollmentRole;
import com.project.apsas.repository.CourseContentRepository;
import com.project.apsas.repository.EnrollmentRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.TeacherStatsService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherStatsServiceImpl implements TeacherStatsService {

    EnrollmentRepository enrollmentRepository;
    CourseContentRepository courseContentRepository;
    AuthService authService;

    @Override
    public TeacherStatsDTO getTeacherStats() {
        // Get current teacher ID
        String currentIdStr = authService.currentId();
        Long teacherId = Long.parseLong(currentIdStr);

        // Get all courses where current user is LECTURER
        List<Enrollment> teacherEnrollments = enrollmentRepository
                .findByUserIdAndRole(teacherId, EnrollmentRole.OWNER);

        // Extract course IDs
        List<Long> courseIds = teacherEnrollments.stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return TeacherStatsDTO.builder()
                    .totalStudents(0)
                    .totalCourses(0)
                    .totalLessons(0)
                    .build();
        }

        // Count total students in teacher's courses
        int totalStudents = enrollmentRepository
                .countStudentsByCourseIds(courseIds);

        // Count total courses
        int totalCourses = courseIds.size();

        // Count total lessons (Content) in teacher's courses
        int totalLessons = courseContentRepository
                .countContentsByCourseIds(courseIds);

        return TeacherStatsDTO.builder()
                .totalStudents(totalStudents)
                .totalCourses(totalCourses)
                .totalLessons(totalLessons)
                .build();
    }
}
