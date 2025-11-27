package com.project.apsas.service;


import com.project.apsas.dto.response.submission.StudentSubmissionDTO;
import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.request.CreateSubmissionRequest;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.dto.response.CreateSubmissionResponse;
import com.project.apsas.dto.response.submission.SubmissionDetailResponse;
import com.project.apsas.dto.response.submission.SubmittedAssigmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service to handle submission queries for teachers
 */

public interface SubmissionService {
    public void updataFeedbackByAI(Long submissionId, CodeFeedbackDTO codeFeedbackDTO, boolean status);
    public void updataReportConfig(Long submissionId, ReportCongfigSubmission reportCongfigSubmission, boolean status);

    public Page<StudentSubmissionDTO> getStudentSubmissionsByAssignment(
            Long courseId,
            Long assignmentId,
            Pageable pageable
    );

    public SubmissionDetailResponse getSubmissionDetailForStudent(Long submissionId);
    public SubmissionDetailResponse getSubmissionDetailForTeacher(Long submissionId);
    public SubmittedAssigmentResponse getSubmissionHistory( Long courseId, Long assignmentId,  Long studentId);
    public CreateSubmissionResponse createSubmission(CreateSubmissionRequest createSubmissionRequest);
}
