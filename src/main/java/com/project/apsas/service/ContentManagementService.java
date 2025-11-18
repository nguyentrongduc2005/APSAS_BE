package com.project.apsas.service;

import com.project.apsas.dto.request.admin.ReviewContentRequest;
import com.project.apsas.dto.response.admin.ContentManagementResponse;
import com.project.apsas.dto.response.admin.ContentStatisticsResponse;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentManagementService {
    /**
     * Lấy danh sách content với phân trang và filter
     */
    Page<ContentManagementResponse> getAllContents(
            Pageable pageable, 
            String search, 
            ContentStatus status, 
            MediaType mediaType
    );

    /**
     * Lấy chi tiết content
     */
    ContentManagementResponse getContentById(Long contentId);

    /**
     * Duyệt hoặc từ chối content
     */
    ContentManagementResponse reviewContent(Long contentId, ReviewContentRequest request);

    /**
     * Xóa content
     */
    void deleteContent(Long contentId);

    /**
     * Lấy thống kê content
     */
    ContentStatisticsResponse getContentStatistics();
}
