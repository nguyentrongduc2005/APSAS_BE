package com.project.apsas.service;

import com.project.apsas.dto.student.StudentCourseInsightDto;

import java.util.List;

public interface StudentInsightService {

    List<StudentCourseInsightDto> getMyCourseInsights();
}
