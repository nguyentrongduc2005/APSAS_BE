package com.project.apsas.service.impl;

import com.project.apsas.dto.student.DailyScoreDTO;
import com.project.apsas.dto.student.ProgressDTO;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.EnrollmentRepository;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    SubmissionRepository submissionRepository;
    EnrollmentRepository enrollmentRepository;


    @Transactional
    public ProgressDTO getStudentCurrentProgress(Long studentId) {

        var user = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        long totalCourses = enrollmentRepository.countByStudentId(studentId);

        Double averageScore = Optional.ofNullable(
                submissionRepository.findAverageScore(studentId)
        ).orElse(0.0);

        LocalDateTime fromDate = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime toDate = LocalDate.now().atTime(23,59,59);

        List<DailyScoreDTO> last7DaysScores =
                submissionRepository.findScoresByDateRange(studentId, fromDate, toDate);

        long completedCourses = submissionRepository.countCompletedAssignments(studentId);

        return ProgressDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .totalCourses(totalCourses)
                .completedCourses((int) completedCourses)
                .averageScore(averageScore)
                .dailyScoreDTOList(last7DaysScores)
                .build();
    }
    public List<DailyScoreDTO> getStudentDailyScores(Long studentId, LocalDate from, LocalDate to){
// Validate input
        if (from == null || to == null) {
            throw new AppException(ErrorCode.DATE_INVALID, "fromDate và toDate không được null");
        }

        if (from.isAfter(to)) {
            throw new AppException(ErrorCode.DATE_INVALID, "fromDate phải nhỏ hơn hoặc bằng toDate");
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to);
        if (days > 30) {
            throw new AppException(ErrorCode.DATE_INVALID, "Khoảng ngày không được vượt quá 30 ngày");
        }

        userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy user"));

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        return submissionRepository.findScoresByDateRange(
                studentId,
                fromDateTime,
                toDateTime
        );
    }


}
