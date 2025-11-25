package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/content")
public class ContentController {

    // Note: Content creation is handled through TutorialController
    // This endpoint is deprecated - use POST /tutorials/{tutorialId}/contents instead
    
    @Deprecated
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CREATE_CONTENT')")
    public ApiResponse<Object> createContent(@RequestBody Object request){
        return ApiResponse.builder()
                .code("deprecated")
                .message("This endpoint is deprecated. Use POST /tutorials/{tutorialId}/contents instead")
                .build();
    }
}
