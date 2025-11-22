package com.project.apsas.service;

import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.content.CreateContentRequest;
import com.project.apsas.dto.request.content.UpdateContentRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.content.CreateContentResponse;
import com.project.apsas.dto.response.content.UpdateContentResponse;
import com.project.apsas.dto.response.tutorial.DetailAssignmentResponse;
import com.project.apsas.dto.response.tutorial.DetailContentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContentService {

    public CreateContentResponse createContent(Long tutorialId ,CreateContentRequest request, List<MultipartFile> files);
    public UpdateContentResponse updateContent(Long tutorialId ,
                                               UpdateContentRequest request,
                                               List<MultipartFile> filesAdd
    );

    public DetailContentResponse detailContentTutorial(Long contentId);


}
