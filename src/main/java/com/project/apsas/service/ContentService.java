package com.project.apsas.service;

import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.content.CreateContentRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.content.CreateContentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContentService {

    public CreateContentResponse createContent(Long tutorialId ,CreateContentRequest request, List<MultipartFile> files);

}
