package com.project.apsas.service;

import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.SubmissionResponse;
import com.project.apsas.dto.StudentSubmissionDTO;
import com.project.apsas.entity.Submission;
import com.project.apsas.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to handle submission queries for teachers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    /**
     * Lấy tất cả submissions trong một course (dành cho giáo viên)
     * @param courseId Course ID
     * @param page Page number (1-based)
     * @param limit Items per page
     * @return Paginated submissions
     */
    public PagedResponse<SubmissionResponse> getSubmissionsByCourse(
            Long courseId,
            int page,
            int limit
    ) {
        // Validate parameters
        int pg = Math.max(page, 1);
        int lm = Math.min(Math.max(limit, 1), 100);  // Max 100 per page
        
        Pageable pageable = PageRequest.of(pg - 1, lm);
        Page<Submission> submissions = submissionRepository.findByCourseId(courseId, pageable);

        List<SubmissionResponse> data = submissions.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        int totalPages = submissions.getTotalPages();
        boolean hasNext = submissions.hasNext();
        boolean hasPrev = submissions.hasPrevious();

        return PagedResponse.<SubmissionResponse>builder()
                .data(data)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(submissions.getTotalElements())
                        .totalPages(totalPages)
                        .hasNext(hasNext)
                        .hasPrev(hasPrev)
                        .build())
                .build();
    }

    /**
     * Lấy submissions của một assignment trong course
     * @param courseId Course ID
     * @param assignmentId Assignment ID
     * @param page Page number (1-based)
     * @param limit Items per page
     * @return Paginated submissions
     */
    public PagedResponse<SubmissionResponse> getSubmissionsByAssignment(
            Long courseId,
            Long assignmentId,
            int page,
            int limit
    ) {
        int pg = Math.max(page, 1);
        int lm = Math.min(Math.max(limit, 1), 100);
        
        Pageable pageable = PageRequest.of(pg - 1, lm);
        Page<Submission> submissions = submissionRepository
                .findByAssignmentIdAndCourseId(assignmentId, courseId, pageable);

        List<SubmissionResponse> data = submissions.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        int totalPages = submissions.getTotalPages();
        boolean hasNext = submissions.hasNext();
        boolean hasPrev = submissions.hasPrevious();

        return PagedResponse.<SubmissionResponse>builder()
                .data(data)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(submissions.getTotalElements())
                        .totalPages(totalPages)
                        .hasNext(hasNext)
                        .hasPrev(hasPrev)
                        .build())
                .build();
    }

    /**
     * Lấy chi tiết submission của một student
     * @param assignmentId Assignment ID
     * @param studentId Student ID
     * @return Submission details
     */
    public SubmissionResponse getSubmissionDetail(Long assignmentId, Long studentId) {
        var result = submissionRepository.findByAssignmentIdAndUserId(assignmentId, studentId);
        
        if (result.isEmpty()) {
            throw new RuntimeException("Submission not found");
        }

        return mapToResponse(result.get());
    }

    /**
     * Map Submission entity to SubmissionResponse DTO
     */
    private SubmissionResponse mapToResponse(Submission submission) {
        String codePreview = null;
        Integer codeLength = null;

        if (submission.getCode() != null) {
            codeLength = submission.getCode().length();
            codePreview = submission.getCode().substring(0, Math.min(500, codeLength));
        }

        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .assignmentTitle(submission.getAssignment() != null ? submission.getAssignment().getTitle() : null)
                .studentId(submission.getUserId())
                .studentName(submission.getUser() != null ? submission.getUser().getName() : null)
                .studentEmail(submission.getUser() != null ? submission.getUser().getEmail() : null)
                .language(submission.getLanguage())
                .score(submission.getScore())
                .passed(submission.getPassed())
                .feedback(submission.getFeedback())
                .attemptNo(submission.getAttemptNo())
                .submittedAt(submission.getSubmittedAt())
                .codePreview(codePreview)
                .codeLength(codeLength)
                .reportJson(submission.getReportJson())
                .build();
    }

    /**
     * Lấy tất cả submissions của một student
     */
    public List<SubmissionResponse> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByUserId(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lấy submissions của một assignment
     */
    public List<SubmissionResponse> getSubmissionsByAssignmentId(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lấy danh sách sinh viên đã nộp bài trong một assignment của course
     * @param courseId Course ID
     * @param assignmentId Assignment ID
     * @param page Page number (1-based)
     * @param limit Items per page
     * @return Paginated student submissions
     */
    public PagedResponse<StudentSubmissionDTO> getStudentSubmissionsByAssignment(
            Long courseId,
            Long assignmentId,
            int page,
            int limit
    ) {
        // Validate parameters
        int pg = Math.max(page, 1);
        int lm = Math.min(Math.max(limit, 1), 100);  // Max 100 per page
        
        Pageable pageable = PageRequest.of(pg - 1, lm);
        Page<Object[]> results = submissionRepository.findStudentSubmissionsByCourseAndAssignment(
                courseId, assignmentId, pageable
        );

        List<StudentSubmissionDTO> data = results.getContent().stream()
                .map(obj -> StudentSubmissionDTO.builder()
                        .studentId((Long) obj[0])
                        .studentName((String) obj[1])
                        .studentEmail((String) obj[2])
                        .score(obj[3] != null ? new java.math.BigDecimal(obj[3].toString()) : null)
                        .passed((Boolean) obj[4])
                        .submittedAt((java.time.LocalDateTime) obj[5])
                        .attemptNo((Integer) obj[6])
                        .assignmentTitle((String) obj[7])
                        .build())
                .toList();

        int totalPages = results.getTotalPages();
        boolean hasNext = results.hasNext();
        boolean hasPrev = results.hasPrevious();

        return PagedResponse.<StudentSubmissionDTO>builder()
                .data(data)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(results.getTotalElements())
                        .totalPages(totalPages)
                        .hasNext(hasNext)
                        .hasPrev(hasPrev)
                        .build())
                .build();
    }
}
