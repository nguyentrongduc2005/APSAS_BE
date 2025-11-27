package com.project.apsas.service;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.request.tutorial.UpdateTutorialRequest;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.dto.response.tutorial.DetailTutorialResponse;
import com.project.apsas.dto.response.tutorial.SearchTutorialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TutorialService {
    public CreateTutorialResponse createTutorial(CreateTutorialRequest request);
    // API lấy list tutorial của chính provider hiện tại
    /**
     * Lấy danh sách tutorial của chính user hiện tại
     *
     * @param keyword       chuỗi tìm kiếm theo title/summary (có thể null)
     * @param status        filter theo status (PUBLISHED/DRAFT/ARCHIVED) - có thể null
     * @param hasAssignment true = chỉ lấy tutorial có assignment, false = chỉ lấy tutorial không có assignment, null = bỏ qua filter này
     */
    Page<SearchTutorialResponse> getMyTutorials(String keyword, String status, Boolean hasAssignment, Pageable pageable);

    public Boolean updateTutorial(UpdateTutorialRequest request, Long tutorialId);

    // API public list tutorial (tìm kiếm + phân trang)
    Page<SearchTutorialResponse> searchTutorials(String keyword, Pageable pageable);

    public DetailTutorialResponse getTutorialDetail(Long tutorialId);
    
    // Submit tutorial for admin review
    public Boolean submitTutorialForReview(Long tutorialId);
}
