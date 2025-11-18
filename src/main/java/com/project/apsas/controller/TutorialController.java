package com.project.apsas.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.assignment.UpdateAssignmentRequest;
import com.project.apsas.dto.request.content.CreateContentRequest;
import com.project.apsas.dto.request.content.UpdateContentRequest;
import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.request.tutorial.UpdateTutorialRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.content.CreateContentResponse;
import com.project.apsas.dto.response.content.UpdateContentResponse;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.dto.response.tutorial.DetailContentResponse;
import com.project.apsas.dto.response.tutorial.DetailTutorialResponse;
import com.project.apsas.service.AssignmentService;
import com.project.apsas.service.ContentService;
import com.project.apsas.service.TutorialService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


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
    @PostMapping(value = "/{tutorialId}/contents",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ApiResponse<CreateContentResponse> createContentForTutorial(
            @PathVariable Long tutorialId,
            @RequestParam("orderNo") Integer orderNo,
            @RequestParam("title") String title,
            @RequestParam("bodyMd") String bodyMd,
            // Thêm @Valid nếu bạn dùng validation
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        CreateContentRequest request = CreateContentRequest.builder()
                .orderNo(orderNo)
                .title(title)
                .bodyMd(bodyMd)
                .build();
        return ApiResponse.<CreateContentResponse>builder()
                .code("ok")
                .message("successfully created tutorial")
                .data(contentService.createContent(tutorialId,request,files))
                .build();
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping(value = "/{tutorialId}/assignments")
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
    @PreAuthorize("hasRole('PROVIDER')") // hoặc PROVIDER
    @PostMapping("/my")
    public ApiResponse<List<CreateTutorialResponse>> getMyTutorials(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean hasAssignment
    ) {
        List<CreateTutorialResponse> data = tutorialService.getMyTutorials(keyword, status, hasAssignment);

        return ApiResponse.<List<CreateTutorialResponse>>builder()
                .code("ok")
                .message("success")
                .data(data)
                .build();
    }
    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/{tutorialId}")
    public ApiResponse<Boolean> updateTutorial(@RequestBody UpdateTutorialRequest request,
                                               @PathVariable Long tutorialId){
        return ApiResponse.<Boolean>builder()
                .code("ok")
                .message("successfully updated tutorial")
                .data(tutorialService.updateTutorial(request,tutorialId))
                .build();
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping(value = "/contents/{contentId}",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ApiResponse<UpdateContentResponse> updateContentForTutorial(
            @PathVariable Long contentId,
            @RequestParam("title") String title,
            @RequestParam("bodyMd") String bodyMd,
            @RequestParam("orderNo") Integer orderNo,
            // (Lưu ý: @RequestParam cũng nhận được List)
            @RequestParam(value = "mediaIdsToDelete", required = false) List<Long> mediaIdsToDelete, // Dùng DTO mới
            @RequestPart(value = "filesAdd", required = false) List<MultipartFile> filesAdd // File để THÊM
    ) {
        UpdateContentRequest request = new UpdateContentRequest();
        request.setTitle(title);
        request.setBodyMd(bodyMd);
        request.setOrderNo(orderNo);
        request.setFilesDelete(mediaIdsToDelete);
        return ApiResponse.<UpdateContentResponse>builder()
                .code("ok")
                .message("successfully updated content")
                .data(contentService.updateContent(contentId, request, filesAdd))
                .build();
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping("/assignments/{assignmentId}")
    public ApiResponse<CreateAssignmentResponse> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody UpdateAssignmentRequest request // Dùng DTO mới
    ) throws JsonProcessingException {

        return ApiResponse.<CreateAssignmentResponse>builder()
                .code("ok")
                .message("Successfully updated assignment")
                // Gọi hàm service mới, trả về DTO giống 'create'
                .data(assignmentService.updateAssignment(assignmentId, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DetailTutorialResponse> getTutorialDetail(@PathVariable Long id) {
        return ApiResponse.<DetailTutorialResponse>builder()
                .code("ok")
                .message("successfully get tutorial")
                .data(tutorialService.getTutorialDetail(id))
                .build();
    }

    @GetMapping("/contents/{contentId}")
    public ApiResponse<DetailContentResponse> getContentDetail(@PathVariable Long contentId) {
        return ApiResponse.<DetailContentResponse>builder()
                .code("ok")
                .message("successfully get tutorial")
                .data(contentService.detailContentTutorial(contentId))
                .build();
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping("/{tutorialId}/submit")
    public ApiResponse<Boolean> submitTutorialForReview(@PathVariable Long tutorialId) {
        return ApiResponse.<Boolean>builder()
                .code("ok")
                .message("Tutorial submitted for review successfully")
                .data(tutorialService.submitTutorialForReview(tutorialId))
                .build();
    }
    @GetMapping
    public ApiResponse<Page<CreateTutorialResponse>> getAllTutorials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc", required = false) String[] sort,
            @RequestParam(required = false) String search
    ) {
        Sort sortObj = createSortObject(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<CreateTutorialResponse> data = tutorialService.searchTutorials(search, pageable);

        return ApiResponse.<Page<CreateTutorialResponse>>builder()
                .code("ok")
                .message("SUCCESS")
                .data(data)
                .build();
    }
    private Sort createSortObject(String[] sort) {
        Sort sortList = Sort.unsorted();
        for (String s : sort) {
            String[] parts = s.split(",");
            if (parts.length == 2) {
                String property = parts[0].trim();
                Sort.Direction direction = "desc".equalsIgnoreCase(parts[1].trim())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                sortList = sortList.and(Sort.by(direction, property));
            }
        }
        return sortList;
    }
}
