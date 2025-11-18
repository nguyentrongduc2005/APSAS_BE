package com.project.apsas.service;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.request.tutorial.UpdateTutorialRequest;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.dto.response.tutorial.DetailTutorialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TutorialService {
    public CreateTutorialResponse createTutorial(CreateTutorialRequest request);
    // API lấy list tutorial của chính provider hiện tại
    List<CreateTutorialResponse> getMyTutorials();
    public Boolean updateTutorial(UpdateTutorialRequest request, Long tutorialId);

    // API public list tutorial (tìm kiếm + phân trang)
    Page<CreateTutorialResponse> searchTutorials(String keyword, Pageable pageable);

    public DetailTutorialResponse getTutorialDetail(Long tutorialId);
    
    // Submit tutorial for admin review
    public Boolean submitTutorialForReview(Long tutorialId);
}
