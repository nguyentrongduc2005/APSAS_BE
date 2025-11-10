package com.project.apsas.service.impl;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.apsas.dto.teacher.TeacherCourseSummaryResponse;
import com.project.apsas.repository.CourseRepository;
import com.project.apsas.service.teacher.TeacherCourseQueryService;

import lombok.RequiredArgsConstructor;

@Service
@Profile("db")
@RequiredArgsConstructor
public class TeacherCourseQueryServiceImpl implements TeacherCourseQueryService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TeacherCourseSummaryResponse> getByTeacherId(Long teacherId) {
        if (teacherId == null) return List.of();
        return courseRepository.findSummariesByTeacherId(teacherId);
    }
}
