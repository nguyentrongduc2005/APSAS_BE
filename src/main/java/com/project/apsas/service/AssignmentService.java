package com.project.apsas.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;

public interface AssignmentService {
    public CreateAssignmentResponse createAssignment(Long tutorialId , CreateAssigmentRequest request) throws JsonProcessingException;
}
