package com.project.apsas.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.apsas.dto.response.assignment.AssignmentDetailDTO;
import com.project.apsas.dto.request.assignment.AssignmentListItemDTO;
import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.assignment.UpdateAssignmentRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.tutorial.DetailAssignmentResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentService {
    public CreateAssignmentResponse createAssignment(Long tutorialId , CreateAssigmentRequest request)
            throws JsonProcessingException;
    public CreateAssignmentResponse updateAssignment(Long assignmentId , UpdateAssignmentRequest request)
            throws JsonProcessingException;
    public void setTime(Long assignmentId,Long courseId, LocalDateTime openAt, LocalDateTime dueAt);

    List<AssignmentListItemDTO> getAssignmentsByCourseId(Long courseId);

    AssignmentDetailDTO getAssignmentDetail(Long courseId, Long assignmentId);

    public DetailAssignmentResponse detailAssignmentTutorial(Long assignmentId) throws JsonProcessingException;

}
