package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.admin.ReviewTutorialRequest;
import com.project.apsas.dto.response.admin.TutorialManagementResponse;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.service.TutorialManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/tutorials")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TutorialManagementController {

    TutorialManagementService tutorialManagementService;

    /**
     * Lấy danh sách tutorials chờ duyệt (PENDING)
     * GET /api/admin/tutorials/pending
     */
    @PreAuthorize("hasAuthority('MANAGE_TUTORIALS')")
    @GetMapping("/pending")
    public ApiResponse<Page<TutorialManagementResponse>> getPendingTutorials(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ApiResponse.<Page<TutorialManagementResponse>>builder()
                .code("ok")
                .message("Lấy danh sách tutorials chờ duyệt thành công")
                .data(tutorialManagementService.getAllTutorials(TutorialStatus.PENDING, keyword, pageable))
                .build();
    }

    @PreAuthorize("hasAuthority('MANAGE_TUTORIALS')")
    @GetMapping("/{tutorialId}")
    public ApiResponse<TutorialManagementResponse> getTutorialDetail(@PathVariable Long tutorialId) {
        return ApiResponse.<TutorialManagementResponse>builder()
                .code("ok")
                .message("Get tutorial detail successfully")
                .data(tutorialManagementService.getTutorialDetail(tutorialId))
                .build();
    }

    /**
     * Duyệt tutorial - chuyển trạng thái thành PUBLISHED
     * PUT /api/admin/tutorials/{tutorialId}/publish
     */
    @PreAuthorize("hasAuthority('PUBLISH_TUTORIALS')")
    @PutMapping("/{tutorialId}/publish")
    public ApiResponse<TutorialManagementResponse> publishTutorial(@PathVariable Long tutorialId) {
        return ApiResponse.<TutorialManagementResponse>builder()
                .code("ok")
                .message("Tutorial đã được phát hành thành công")
                .data(tutorialManagementService.publishTutorial(tutorialId))
                .build();
    }

    /**
     * Duyệt hoặc từ chối tutorial - chuyển trạng thái thành PUBLISHED hoặc REJECTED
     * PUT /api/admin/tutorials/{tutorialId}/review
     */
    @PreAuthorize("hasAuthority('MANAGE_TUTORIALS')")
    @PutMapping("/{tutorialId}/review")
    public ApiResponse<TutorialManagementResponse> reviewTutorial(
            @PathVariable Long tutorialId,
            @Valid @RequestBody ReviewTutorialRequest request
    ) {
        TutorialManagementResponse response = tutorialManagementService.reviewTutorial(tutorialId, request);
        String message = request.getStatus() == TutorialStatus.PUBLISHED
                ? "Tutorial đã được duyệt thành công"
                : "Tutorial đã bị từ chối";
        
        return ApiResponse.<TutorialManagementResponse>builder()
                .code("ok")
                .message(message)
                .data(response)
                .build();
    }
}
