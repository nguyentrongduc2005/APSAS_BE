package com.project.apsas.service.impl;

import com.project.apsas.dto.student.StudentCourseInsightDto;
import com.project.apsas.repository.SubmissionInsightRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.StudentInsightService;
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
public class StudentInsightServiceImpl implements StudentInsightService {

    SubmissionInsightRepository submissionInsightRepository;
    AuthService authService;

    @Override
    public List<StudentCourseInsightDto> getMyCourseInsights() {
        // Lấy id current user từ JWT (giống TeacherStatsServiceImpl)
        Long studentId = Long.parseLong(authService.currentId());
        return submissionInsightRepository.findStudentCourseInsights(studentId);
    }
}
