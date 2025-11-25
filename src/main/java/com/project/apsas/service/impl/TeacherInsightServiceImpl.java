package com.project.apsas.service.impl;

import com.project.apsas.dto.teacher.TeacherCourseInsightDto;
import com.project.apsas.repository.SubmissionInsightRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.TeacherInsightService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherInsightServiceImpl implements TeacherInsightService {

    SubmissionInsightRepository submissionInsightRepository;
    AuthService authService;

    @Override
    public List<TeacherCourseInsightDto> getMyCourseInsights() {
        Long teacherId = Long.parseLong(authService.currentId());
        return submissionInsightRepository.findTeacherCourseInsights(teacherId);
    }
}
