package com.project.apsas.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.StudentSubmissionDTO;
import com.project.apsas.dto.event.FeedbackEvent;
import com.project.apsas.dto.event.SubmitCodeEvent;
import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.request.CreateSubmissionRequest;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.dto.response.CreateSubmissionResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.SubmissionResponse;
import com.project.apsas.entity.*;
import com.project.apsas.enums.StatusSubmission;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.integration.kafka.ai.KafkaFeedbackProvider;
import com.project.apsas.integration.kafka.jubge.KafkaRCEProducer;
import com.project.apsas.mapper.SubmissionMapper;
import com.project.apsas.repository.*;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.SubmissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionServiceImpl implements SubmissionService {
    SubmissionRepository submissionRepository;
    SubmissionMapper submissionMapper;
    ObjectMapper objectMapper;
    AuthService authService;
    UserRepository userRepository;

    CourseAssignmentRepository courseAssignmentRepository;
    EnrollmentRepository enrollmentRepository;
    ProgressSkillRepository progressSkillRepository;

    KafkaRCEProducer kafkaRCEProducer;

    KafkaFeedbackProvider kafkaFeedbackProvider;

    @NonFinal
    @Value("${message-queue.topic.feedback.name}")
    String feedbackTopic;
    @NonFinal
    @Value("${message-queue.topic.execute.name}")
    String executeTopic;

    @Override
    public void updataReportConfig(Long submissionId, ReportCongfigSubmission reportCongfigSubmission, boolean status) {
        if(!status) {
            Submission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));
            ProgressSkill progressSkill = progressSkillRepository.findById(
                    new ProgressSkillId(submission.getUserId(),
                    submission.getAssignment().getSkillId())
            ).orElse(null);
            int profinciency = submission.getAssignment().getProficiency();

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
                if(progressSkill != null) {
                    progressSkill.setScore(score.multiply(new BigDecimal(profinciency)));
                    progressSkill.setLevel(progressSkill.getScore().intValue() % 1000);
                    progressSkillRepository.save(progressSkill);
                }

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
                .assignmentId(submission.getAssignment().getId())
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
     * @param pageable Pageable object (page, size, sort)
     * @return Paginated student submissions
     */
    @Override
    public Page<StudentSubmissionDTO> getStudentSubmissionsByAssignment(
            Long courseId,
            Long assignmentId,
            Pageable pageable
    ) {
        Page<Object[]> results = submissionRepository.findStudentSubmissionsByCourseAndAssignment(
                courseId, assignmentId, pageable
        );

        return results.map(obj -> StudentSubmissionDTO.builder()
                .studentId((Long) obj[0])
                .studentName((String) obj[1])
                .studentEmail((String) obj[2])
                .score(obj[3] != null ? new BigDecimal(obj[3].toString()) : null)
                .passed((Boolean) obj[4])
                .submittedAt((LocalDateTime) obj[5])
                .attemptNo((Integer) obj[6])
                .build());
    }

    @Override
    public CreateSubmissionResponse createSubmission(CreateSubmissionRequest req) {
        Long userId = Long.parseLong(authService.currentId());
        Long assignmentId = req.getAssignmentId();
        Long courseId = req.getCourseId();

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if(!enrollmentRepository.existsEnrollmentByCourseIdAndUserId(courseId, userId))
            throw new AppException(ErrorCode.BAD_REQUEST);

        if(!courseAssignmentRepository.existsCourseAssignmentByAssignmentIdAndCourseId(assignmentId, courseId))
            throw new AppException(ErrorCode.BAD_REQUEST);

        CourseAssignment courseAssignment = courseAssignmentRepository.findById(
                new CourseAssignment.PK(courseId, assignmentId)
        ).orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));
        if(courseAssignment.getOpenAt().isAfter(LocalDateTime.now()))
            throw new AppException(ErrorCode.ASSIGNMENT_NOT_OPEN);

        if(courseAssignment.getDueAt().isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.ASSIGNMENT_HAVE_CLOSE);

        Assignment assignment = courseAssignment.getAssignment();

        int attempt = submissionRepository.countByCourseIdAndAssignmentIdAndUserId(courseId, assignmentId, userId);
        if(attempt >= assignment.getAttemptsLimit())
            throw new AppException(ErrorCode.BAD_REQUEST);
        Submission submission = Submission.builder()
                .code(req.getCode())
                .userId(userId)
                .courseId(courseId)
                .assignmentId(assignment.getId())
                .language(String.valueOf(req.getLanguageId()))
                .attemptNo(attempt + 1)
                .status(StatusSubmission.PENDING)
                .build();
        submissionRepository.save(submission);
        // add skill cho user
        if(!(user.getProgress() == null)) {
            ProgressSkill progressSkill = ProgressSkill.builder()
                    .skillId(assignment.getSkill().getId())
                    .progressId(user.getProgress().getId())
                    .level(1)
                    .score(BigDecimal.valueOf(0))
                    .build();
            progressSkillRepository.save(progressSkill);
        }
        if(!assignment.getAssignmentEvaluations().stream().findFirst().isPresent())
            throw new AppException(ErrorCode.BAD_REQUEST);
        FeedbackEvent feedbackEvent = FeedbackEvent.builder()
                .submissionId(submission.getId())
                .code(req.getCode())
                .statement_md(assignment.getStatementMd())
                .language(submission.getLanguage())
                .build();
        SubmitCodeEvent submitCodeEvent = SubmitCodeEvent.builder()
                .code(req.getCode())
                .submissionId(submission.getId())
                .configJson(assignment.getAssignmentEvaluations().stream().findFirst().get().getConfigJson())
                .languageId(Integer.parseInt(submission.getLanguage()))
                .build();
        kafkaRCEProducer.push(executeTopic,submitCodeEvent.getSubmissionId().toString(),submitCodeEvent);

        kafkaFeedbackProvider.push(feedbackTopic,submitCodeEvent.getSubmissionId().toString(),feedbackEvent);

        return CreateSubmissionResponse.builder()
                .submissionId(submission.getId())
                .build();
    }

    /**
     * Lấy tất cả submissions của một student trong course
     * Bao gồm cả assignments chưa nộp
     */
    @Override
    public Page<com.project.apsas.dto.StudentAllSubmissionsDTO> getAllSubmissionsOfStudent(
            Long courseId,
            Long studentId,
            Pageable pageable
    ) {
        // 1. Verify enrollment exists
        Enrollment.PK enrollmentPK = new Enrollment.PK(studentId, courseId);
        if (!enrollmentRepository.existsById(enrollmentPK)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 2. Lấy tất cả assignments của course (không phân trang ở đây)
        List<CourseAssignment> allCourseAssignments = courseAssignmentRepository
                .findAllByCourseId(courseId);
        
        // 3. Map sang DTO với thông tin submission (nếu có)
        List<com.project.apsas.dto.StudentAllSubmissionsDTO> allSubmissions = allCourseAssignments.stream()
                .map(ca -> {
                    Assignment assignment = ca.getAssignment();
                    
                    // Tìm submission của student cho assignment này (lấy latest submission)
                    Optional<Submission> submissionOpt = submissionRepository
                            .findTopByAssignmentIdAndUserIdOrderBySubmittedAtDesc(
                                    assignment.getId(), 
                                    studentId
                            );
                    
                    com.project.apsas.dto.StudentAllSubmissionsDTO.StudentAllSubmissionsDTOBuilder builder = 
                            com.project.apsas.dto.StudentAllSubmissionsDTO.builder()
                                // Assignment info
                                .assignmentId(assignment.getId())
                                .assignmentTitle(assignment.getTitle())
                                .assignmentDescription(assignment.getStatementMd())
                                .assignmentMaxScore(assignment.getMaxScore() != null ? assignment.getMaxScore().intValue() : null)
                                .language(null); // Assignment không có language field
                    
                    if (submissionOpt.isPresent()) {
                        Submission submission = submissionOpt.get();
                        
                        builder
                            // Submission info
                            .submissionId(submission.getId())
                            .status(submission.getStatus() != null ? submission.getStatus().name() : null)
                            .score(submission.getScore() != null ? submission.getScore().doubleValue() : null)
                            .submittedAt(submission.getSubmittedAt())
                            .feedback(submission.getFeedback())
                            .attemptNo(submission.getAttemptNo())
                            .passed(submission.getPassed())
                            .language(submission.getLanguage())
                            // Derived fields
                            .hasSubmitted(true);
                    } else {
                        // Chưa nộp bài
                        builder
                            .submissionId(null)
                            .status(null)
                            .score(null)
                            .submittedAt(null)
                            .feedback(null)
                            .attemptNo(null)
                            .passed(null)
                            .hasSubmitted(false);
                    }
                    
                    return builder.build();
                })
                .toList();
        
        // 4. Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allSubmissions.size());
        
        List<com.project.apsas.dto.StudentAllSubmissionsDTO> pageContent = 
                allSubmissions.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
                pageContent, 
                pageable, 
                allSubmissions.size()
        );
    }

    /**
     * Student xem các assignment đã nộp của chính mình
     */
    @Override
    public Page<com.project.apsas.dto.StudentSubmittedAssignmentDTO> getMySubmittedAssignments(
            Pageable pageable
    ) {
        // 1. Lấy user ID hiện tại
        Long currentUserId = Long.parseLong(authService.currentId());
        
        // 2. Lấy tất cả submissions của user với pagination
        Page<Submission> submissions = submissionRepository
                .findByUserIdOrderBySubmittedAtDesc(currentUserId, pageable);
        
        // 3. Map sang DTO
        return submissions.map(submission -> {
            Assignment assignment = submission.getAssignment();
            Course course = submission.getCourse();
            
            return com.project.apsas.dto.StudentSubmittedAssignmentDTO.builder()
                    // Assignment info
                    .assignmentId(assignment.getId())
                    .assignmentTitle(assignment.getTitle())
                    .assignmentDescription(assignment.getStatementMd())
                    .assignmentMaxScore(assignment.getMaxScore() != null ? assignment.getMaxScore().intValue() : null)
                    // Course info
                    .courseId(course.getId())
                    .courseName(course.getName())
                    // Submission info
                    .submissionId(submission.getId())
                    .status(submission.getStatus() != null ? submission.getStatus().name() : null)
                    .score(submission.getScore() != null ? submission.getScore().doubleValue() : null)
                    .passed(submission.getPassed())
                    .submittedAt(submission.getSubmittedAt())
                    .attemptNo(submission.getAttemptNo())
                    .language(submission.getLanguage())
                    .feedback(submission.getFeedback())
                    // Additional info
                    .isLatest(true) // Vì query đã sort theo submittedAt desc
                    .build();
        });
    }
}
