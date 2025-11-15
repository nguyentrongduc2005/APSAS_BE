package com.project.apsas.service;


import com.project.apsas.dto.StudentSubmissionDTO;
import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.request.CreateSubmissionRequest;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.dto.response.CreateSubmissionResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.SubmissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service to handle submission queries for teachers
 */

public interface SubmissionService {
    public void updataFeedbackByAI(Long submissionId, CodeFeedbackDTO codeFeedbackDTO, boolean status);
    public void updataReportConfig(Long submissionId, ReportCongfigSubmission reportCongfigSubmission, boolean status);
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
    public Page<StudentSubmissionDTO> getStudentSubmissionsByAssignment(
            Long courseId,
            Long assignmentId,
            Pageable pageable
    );
    
    /**
     * Lấy tất cả submissions của một student trong một course
     * Bao gồm cả assignments chưa nộp
     * 
     * @param courseId Course ID
     * @param studentId Student ID
     * @param pageable Pageable object
     * @return Page of all submissions (submitted and not submitted)
     */
    public Page<com.project.apsas.dto.StudentAllSubmissionsDTO> getAllSubmissionsOfStudent(
            Long courseId,
            Long studentId,
            Pageable pageable
    );
    
    public CreateSubmissionResponse createSubmission(CreateSubmissionRequest createSubmissionRequest);
}
