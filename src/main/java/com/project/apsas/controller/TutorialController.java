package com.project.apsas.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.content.CreateContentRequest;
import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.content.CreateContentResponse;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.service.AssignmentService;
import com.project.apsas.service.ContentService;
import com.project.apsas.service.TutorialService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/tutorials")
public class TutorialController {

    TutorialService tutorialService;
    ContentService contentService;
    AssignmentService assignmentService;
    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("/create")
    public ApiResponse<CreateTutorialResponse>  createTutorial(@RequestBody CreateTutorialRequest request){

        return ApiResponse.<CreateTutorialResponse>builder()
                .code("ok")
                .message("successfully created tutorial")
                .data(tutorialService.createTutorial(request))
                .build();
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("/{tutorialId}/contents")
    public ApiResponse<CreateContentResponse> createContentForTutorial(
            @PathVariable Long tutorialId,
            @RequestBody CreateContentRequest request // Thêm @Valid nếu bạn dùng validation
    ) {

        return ApiResponse.<CreateContentResponse>builder()
                .code("ok")
                .message("successfully created tutorial")
                .data(contentService.createContent(tutorialId,request))
                .build();
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("/{tutorialId}/assignments")
    public ApiResponse<CreateAssignmentResponse> createAssignmentForTutorial(
            @PathVariable Long tutorialId,
            @RequestBody CreateAssigmentRequest request // Thêm @Valid nếu bạn dùng validation
    ) throws JsonProcessingException {

        return ApiResponse.<CreateAssignmentResponse>builder()
                .code("ok")
                .message("successfully created tutorial")
                .data(assignmentService.createAssignment(tutorialId,request))
                .build();
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("/my")
    public ApiResponse<List<CreateTutorialResponse>> getMyTutorials() {
        List<CreateTutorialResponse> data = tutorialService.getMyTutorials();

        return ApiResponse.<List<CreateTutorialResponse>>builder()
                .code("ok")
                .message("success")
                .data(data)
                .build();
    }
}
