package com.project.apsas.service.impl;

import com.project.apsas.dto.student.ProgressDTO;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    SubmissionRepository submissionRepository;
    EnrollmentRepository enrollmentRepository;


    @Transactional
    public ProgressDTO getStudentProgress(Long studentId){
        var user = userRepository.findById(studentId).orElseThrow(()-> new RuntimeException("User not found"));
        long totalCourses = enrollmentRepository.countByStudentId(studentId);
        Double averageScore = submissionRepository.findAverageScore(studentId);
        averageScore = averageScore != null ? averageScore : 0.0;
        LocalDate fromDate = LocalDate.now().minusDays(7);
        List<ProgressDTO.DailyScore> last7DaysScores =
                submissionRepository.findLast7DaysScores(studentId, fromDate);

        int completedCourses = last7DaysScores.size(); // tạm tính theo số ngày có điểm

        return new ProgressDTO(
                user.getName(),
                user.getEmail(),
                totalCourses,
                completedCourses,
                averageScore,
                last7DaysScores
        );

    }
}
