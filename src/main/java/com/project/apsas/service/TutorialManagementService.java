package com.project.apsas.service;

import com.project.apsas.dto.response.admin.TutorialManagementResponse;
import com.project.apsas.enums.TutorialStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TutorialManagementService {
    Page<TutorialManagementResponse> getAllTutorials(TutorialStatus status, String keyword, Pageable pageable);
    TutorialManagementResponse getTutorialDetail(Long tutorialId);
    TutorialManagementResponse publishTutorial(Long tutorialId);
    TutorialManagementResponse reviewTutorial(Long tutorialId, com.project.apsas.dto.request.admin.ReviewTutorialRequest request);
}
