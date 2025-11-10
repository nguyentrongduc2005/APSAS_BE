package com.project.apsas.service.impl;

import com.project.apsas.dto.response.StudentCourseCardDto;
import com.project.apsas.dto.response.StudentCourseDetailsDto;
import com.project.apsas.entity.Course;
import com.project.apsas.enums.CourseVisibility;
import com.project.apsas.repository.CourseAssignmentRepository;
import com.project.apsas.repository.CourseRepository;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl implements StudentCourseService {

    private final CourseRepository courseRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public Page<StudentCourseCardDto> myCourses(Long studentId,
                                                String keyword,
                                                CourseVisibility visibility,
                                                Pageable pageable) {
        var page = courseRepository.searchPublic(keyword, visibility, pageable);

        List<StudentCourseCardDto> content = page.getContent().stream().map(c -> {
            Long courseId = c.getId();

            int totalAssignments   = courseAssignmentRepository.countByCourseId(courseId);
            int mySubs             = submissionRepository.countByCourseIdAndUserId(courseId, studentId);
            int myGradedSubs       = submissionRepository.countGradedByCourseIdAndUserId(courseId, studentId);
            int myPending          = Math.max(0, mySubs - myGradedSubs);
            Double avgObj          = submissionRepository.avgScoreByCourseIdAndUserId(courseId, studentId);
            double myAvgScore      = avgObj == null ? 0.0 : round2(avgObj);
            double myCompletion    = totalAssignments == 0 ? 0.0 : round2(myGradedSubs * 1.0 / totalAssignments);

            return StudentCourseCardDto.builder()
                    .id(courseId)
                    .title(c.getName())
                    .visibility(c.getVisibility() != null ? c.getVisibility().name() : null)
                    .totalAssignments(totalAssignments)
                    .mySubmissions(mySubs)
                    .myGradedSubmissions(myGradedSubs)
                    .myPendingSubmissions(myPending)
                    .myAverageScore(myAvgScore)
                    .myCompletionRate(myCompletion)
                    .build();
        }).toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public StudentCourseDetailsDto getCourseDetails(Long studentId, Long courseId) {
        Course c = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Chỉ hiển thị course PUBLIC (theo yêu cầu hiện tại)
        if (c.getVisibility() != CourseVisibility.PUBLIC) {
            throw new RuntimeException("Access denied");
        }

        int totalAssignments   = courseAssignmentRepository.countByCourseId(courseId);
        int mySubs             = submissionRepository.countByCourseIdAndUserId(courseId, studentId);
        int myGradedSubs       = submissionRepository.countGradedByCourseIdAndUserId(courseId, studentId);
        int myPending          = Math.max(0, mySubs - myGradedSubs);
        Double avgObj          = submissionRepository.avgScoreByCourseIdAndUserId(courseId, studentId);
        double myAvgScore      = avgObj == null ? 0.0 : round2(avgObj);
        double myCompletion    = totalAssignments == 0 ? 0.0 : round2(myGradedSubs * 1.0 / totalAssignments);

        return StudentCourseDetailsDto.builder()
                .id(c.getId())
                .title(c.getName())
                .code(c.getCode())
                .visibility(c.getVisibility() != null ? c.getVisibility().name() : null)
                .limit(c.getLimit())
                .createdAt(c.getCreatedAt())
                .totalAssignments(totalAssignments)
                .mySubmissions(mySubs)
                .myGradedSubmissions(myGradedSubs)
                .myPendingSubmissions(myPending)
                .myAverageScore(myAvgScore)
                .myCompletionRate(myCompletion)
                .build();
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
