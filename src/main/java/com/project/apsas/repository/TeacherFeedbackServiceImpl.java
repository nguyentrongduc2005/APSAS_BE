package com.project.apsas.service.impl;

import com.project.apsas.dto.request.TeacherFeedbackCreateRequest;
import com.project.apsas.dto.response.TeacherFeedbackDTO;
import com.project.apsas.entity.Feedback;
import com.project.apsas.entity.Submission;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.FeedbackRepository;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.TeacherFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherFeedbackServiceImpl implements TeacherFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public TeacherFeedbackDTO createTeacherFeedback(Long submissionId,
                                                    TeacherFeedbackCreateRequest request) {

        if (request == null || request.getBody() == null || request.getBody().trim().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Check submission tồn tại
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

        // Dùng bảng feedback hiện có: body + submissionId
        Feedback feedback = Feedback.builder()
                .submissionId(submission.getId())
                .body(request.getBody().trim())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        return TeacherFeedbackDTO.builder()
                .id(saved.getId())
                .body(saved.getBody())
                .createdAt(saved.getCreatedAt())
                .build();
    }


}
