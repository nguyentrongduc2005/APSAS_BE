package com.project.apsas.service;

import java.util.List;

import com.project.apsas.dto.teacher.TeacherCourseSummaryResponse;

public interface TeacherCourseQueryService {
    List<TeacherCourseSummaryResponse> getByTeacherId(Long teacherId);
}
