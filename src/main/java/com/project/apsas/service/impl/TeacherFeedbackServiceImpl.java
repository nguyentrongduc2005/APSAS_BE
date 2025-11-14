package com.project.apsas.service.impl;

import com.project.apsas.dto.teacher.TeacherFeedbackRequest;
import com.project.apsas.dto.teacher.TeacherFeedbackResponse;
import com.project.apsas.entity.TeacherFeedback;
import com.project.apsas.repository.TeacherFeedbackRepository;
import com.project.apsas.service.TeacherFeedbackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherFeedbackServiceImpl implements TeacherFeedbackService {

    TeacherFeedbackRepository teacherFeedbackRepository;

    @Override
    public TeacherFeedbackResponse addFeedback(Long submissionId, TeacherFeedbackRequest request) {

        // Tạo TeacherFeedback entity
        TeacherFeedback feedback = TeacherFeedback.builder()
                .submissionId(submissionId)
                .body(request.getBody())
                .build();

        TeacherFeedback saved = teacherFeedbackRepository.save(feedback);

        return toResponse(saved);
    }

    @Override
    public List<TeacherFeedbackResponse> getFeedbacksBySubmission(Long submissionId) {

        List<TeacherFeedback> feedbacks =
                teacherFeedbackRepository.findBySubmissionId(submissionId).stream()
                        .sorted(Comparator.comparing(
                                TeacherFeedback::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                        .collect(Collectors.toList());

        return feedbacks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TeacherFeedbackResponse toResponse(TeacherFeedback feedback) {
        TeacherFeedbackResponse res = new TeacherFeedbackResponse();
        res.setId(feedback.getId());
        res.setBody(feedback.getBody());
        res.setCreatedAt(feedback.getCreatedAt());
        res.setSubmissionId(feedback.getSubmissionId());
        return res;
    }
}
