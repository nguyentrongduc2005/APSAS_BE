package com.project.apsas.service.impl;

import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.entity.Submission;
import com.project.apsas.enums.StatusSubmission;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.mapper.SubmissionMapper;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.SubmissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionServiceImpl implements SubmissionService {
    SubmissionRepository submissionRepository;
    SubmissionMapper submissionMapper;
    @Override
    public void updataFeedbackByAI(Long submissionId, CodeFeedbackDTO codeFeedbackDTO) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

        if (Objects.isNull(codeFeedbackDTO)) {
            submission.setStatus(StatusSubmission.FAILED);
        } else {
            submissionMapper.updateSubmissionFromDto(codeFeedbackDTO, submission);

            if (submission.getStatus().equals(StatusSubmission.PENDING))
                submission.setStatus(StatusSubmission.PROCESSING);
            else if (submission.getStatus().equals(StatusSubmission.PROCESSING))
                submission.setStatus(StatusSubmission.COMPLETE);
        }
        submissionRepository.save(submission);
    }
}
