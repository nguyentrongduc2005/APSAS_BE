package com.project.apsas.service;

import com.project.apsas.dto.teacher.TeacherCourseInsightDto;

import java.util.List;

public interface TeacherInsightService {

    List<TeacherCourseInsightDto> getMyCourseInsights();
}
