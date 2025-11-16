package com.project.apsas.service.impl;

import com.project.apsas.dto.request.TeacherFeedbackRequest;
import com.project.apsas.dto.response.FeedbackResponse;
import com.project.apsas.entity.Feedback;
import com.project.apsas.entity.Submission;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.FeedbackRepository;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.FeedbackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackServiceImpl implements FeedbackService {

    FeedbackRepository feedbackRepository;
    SubmissionRepository submissionRepository;
    AuthService authService;

    @Override
    public FeedbackResponse addTeacherFeedback(Long submissionId, TeacherFeedbackRequest request) {
        // 1. Check submission tồn tại
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

        // 2. (Optional) Có thể check user hiện tại có phải LECTURER của course này không
        // nhưng ở Controller đã PreAuthorize role rồi, nên ở đây xử lý đơn giản.

        // 3. Tạo feedback
        Feedback feedback = Feedback.builder()
                .body(request.getBody())
                .submissionId(submission.getId())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        return FeedbackResponse.builder()
                .id(saved.getId())
                .body(saved.getBody())
                .createdAt(saved.getCreatedAt())
                .submissionId(saved.getSubmissionId())
                .build();
    }

    @Override
    public List<FeedbackResponse> getFeedbacksOfSubmission(Long submissionId) {
        // Đảm bảo submission tồn tại
        submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

        return feedbackRepository.findBySubmissionIdOrderByCreatedAtAsc(submissionId)
                .stream()
                .map(f -> FeedbackResponse.builder()
                        .id(f.getId())
                        .body(f.getBody())
                        .createdAt(f.getCreatedAt())
                        .submissionId(f.getSubmissionId())
                        .build()
                )
                .toList();
    }
}
