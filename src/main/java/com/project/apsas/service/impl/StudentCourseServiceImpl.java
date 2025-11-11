package com.project.apsas.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.apsas.dto.student.StudentCourseCardDto;
import com.project.apsas.dto.student.StudentCourseDetailsDto;
import com.project.apsas.entity.Course;
import com.project.apsas.enums.CourseVisibility;
import com.project.apsas.repository.CourseAssignmentRepository;
import com.project.apsas.repository.CourseRepository;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.StudentCourseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl implements StudentCourseService {

    private final CourseRepository courseRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<StudentCourseCardDto> myCourses(Long studentId,
                                                String keyword,
                                                CourseVisibility visibility,
                                                Pageable pageable) {
        Page<Course> page = courseRepository.searchPublic(keyword, visibility, pageable);

//        List<StudentCourseCardDto> content = page.getContent().stream().map(c -> {
//            Long courseId = c.getId();
//
//            int totalAssignments = courseAssignmentRepository.countByCourseId(courseId);
//            int mySubs           = submissionRepository.countByCourseIdAndUserId(courseId, studentId);
//            int myGradedSubs     = submissionRepository.countGradedByCourseIdAndUserId(courseId, studentId);
//
//            Double avgObj        = submissionRepository.avgScoreByCourseIdAndUserId(courseId, studentId);
//            double myAvgScore    = avg2(avgObj);
//            double myCompletion  = totalAssignments == 0 ? 0.0 : round2(myGradedSubs * 1.0 / totalAssignments);
//
//            return StudentCourseCardDto.builder()
//                    .id(courseId)
//                    .title(c.getName())
//                    .visibility(c.getVisibility() != null ? c.getVisibility().name() : null)
//                    .totalAssignments(totalAssignments)
//                    .mySubmissions(mySubs)
//                    .myGradedSubmissions(myGradedSubs)
//                    .myAverageScore(myAvgScore)
//                    .myCompletionRate(myCompletion)
//                    .build();
//        }).toList();
//
//        return new PageImpl<>(content, pageable, page.getTotalElements());
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentCourseDetailsDto getCourseDetails(Long studentId, Long courseId) {
        Course c = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("course not found"));

        if (c.getVisibility() != CourseVisibility.PUBLIC) {
            throw new RuntimeException("Access denied");
        }

//        int totalAssignments = courseAssignmentRepository.countByCourseId(courseId);
//        int mySubs           = submissionRepository.countByCourseIdAndUserId(courseId, studentId);
//        int myGradedSubs     = submissionRepository.countGradedByCourseIdAndUserId(courseId, studentId);
//        Double avgObj        = submissionRepository.avgScoreByCourseIdAndUserId(courseId, studentId);
//
//        return StudentCourseDetailsDto.builder()
//                .id(c.getId())
//                .title(c.getName())
//                .visibility(c.getVisibility() != null ? c.getVisibility().name() : null)
//                .totalAssignments(totalAssignments)
//                .mySubmissions(mySubs)
//                .myGradedSubmissions(myGradedSubs)
//                .myAverageScore(avg2(avgObj))
//                .myCompletionRate(totalAssignments == 0 ? 0.0 : round2(myGradedSubs * 1.0 / totalAssignments))
//
        return null;
    }

    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static double avg2(Double v) {
        return v == null ? 0.0 : round2(v);
    }
}
