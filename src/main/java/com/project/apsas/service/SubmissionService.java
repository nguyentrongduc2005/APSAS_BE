package com.project.apsas.service;


import com.project.apsas.dto.StudentSubmissionDTO;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.SubmissionResponse;

import java.util.List;

/**
 * Service to handle submission queries for teachers
 */

public interface SubmissionService {
    public void updataFeedbackByAI(Long submissionId, CodeFeedbackDTO codeFeedbackDTO);
    public PagedResponse<SubmissionResponse> getSubmissionsByCourse(
            Long courseId,
            int page,
            int limit
    );
    public PagedResponse<SubmissionResponse> getSubmissionsByAssignment(
            Long courseId,
            Long assignmentId,
            int page,
            int limit
    );
    public SubmissionResponse getSubmissionDetail(Long assignmentId, Long studentId);
    public List<SubmissionResponse> getSubmissionsByStudent(Long studentId);
    public List<SubmissionResponse> getSubmissionsByAssignmentId(Long assignmentId);
    public PagedResponse<StudentSubmissionDTO> getStudentSubmissionsByAssignment(
            Long courseId,
            Long assignmentId,
            int page,
            int limit
    );
}
