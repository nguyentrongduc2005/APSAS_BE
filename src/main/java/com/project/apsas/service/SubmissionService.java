package com.project.apsas.service;

import com.project.apsas.dto.response.CodeFeedbackDTO;

public interface SubmissionService {
    public void updataFeedbackByAI(Long submissionId, CodeFeedbackDTO codeFeedbackDTO );
}
