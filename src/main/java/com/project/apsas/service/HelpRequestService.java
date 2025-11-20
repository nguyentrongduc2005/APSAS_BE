package com.project.apsas.service;

import com.project.apsas.dto.response.HelpRequestResponse;
import com.project.apsas.dto.response.PagedResponse;

public interface HelpRequestService {
    PagedResponse<HelpRequestResponse> getHelpRequestsByCourse(Long courseId, int page, int limit);
    
    HelpRequestResponse createHelpRequest(Long courseId, String title, String body);
}
