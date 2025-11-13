package com.project.apsas.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.StudentSubmissionDTO;
import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.SubmissionResponse;
import com.project.apsas.entity.Submission;
import com.project.apsas.enums.StatusSubmission;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.mapper.SubmissionMapper;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.SubmissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionServiceImpl implements SubmissionService {
    SubmissionRepository submissionRepository;
    SubmissionMapper submissionMapper;
    ObjectMapper objectMapper;

    @Override
    public void updataReportConfig(Long submissionId, ReportCongfigSubmission reportCongfigSubmission, boolean status) {
        if(!status) {
            Submission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

            // 2. Kiểm tra null (trường hợp RCE bị lỗi)
            if (Objects.isNull(reportCongfigSubmission)) {
                submission.setStatus(StatusSubmission.FAILED);
            } else {

                // 3. Map DTO report vào entity Submission
                int totalCases = reportCongfigSubmission.getTotalTestCases();
                int passCount = reportCongfigSubmission.getPassedTestCases();

                // 3a. Tính toán passed
                submission.setPassed(totalCases > 0 && totalCases == passCount);

                // 3b. Tính toán điểm số
                BigDecimal score = (totalCases > 0)
                        ? new BigDecimal((double) passCount * 100.0 / totalCases).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                submission.setScore(score);

                // 3c. Chuyển toàn bộ report object thành chuỗi JSON để lưu
                try {
                    String reportJsonString = objectMapper.writeValueAsString(reportCongfigSubmission);
                    submission.setReportJson(reportJsonString);
                } catch (JsonProcessingException e) {
                    // Nếu lỗi serialize, coi như quá trình thất bại
                    submission.setStatus(StatusSubmission.FAILED);
                    submission.setReportJson("{\"error\": \"Failed to serialize RCE report\"}");
                }

                // 4. Cập nhật trạng thái (State Machine)
                // Nếu đang là PENDING (mới nộp), thì chuyển sang PROCESSING (chờ AI)
                if (submission.getStatus().equals(StatusSubmission.PENDING)) {
                    submission.setStatus(StatusSubmission.PROCESSING);
                }
            }

            // 5. Lưu lại
            submissionRepository.save(submission);
        }
    }

    @Override
    public void updataFeedbackByAI(Long submissionId, CodeFeedbackDTO codeFeedbackDTO, boolean status) {
        if(!status){
            Submission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

            if (Objects.isNull(codeFeedbackDTO)) {
                submission.setStatus(StatusSubmission.FAILED);
            } else {
                submissionMapper.updateSubmissionFromDto(codeFeedbackDTO, submission);

                if (submission.getStatus().equals(StatusSubmission.PENDING))
                    submission.setStatus(StatusSubmission.PROCESSING);
                else if (submission.getStatus().equals(StatusSubmission.PROCESSING))
                    submission.setStatus(StatusSubmission.COMPLETE);
            }
            submissionRepository.save(submission);
        }
    }
    @Override
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
    @Override
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
    @Override
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
    @Override
    public List<SubmissionResponse> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByUserId(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lấy submissions của một assignment
     */
    @Override
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
    @Override
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
