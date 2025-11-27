package com.project.apsas.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.mapping.ConfigJson;
import com.project.apsas.dto.mapping.TestCase;
import com.project.apsas.dto.mapping.TestCaseResult;
import com.project.apsas.dto.response.submission.StudentSubmissionDTO;
import com.project.apsas.dto.event.FeedbackEvent;
import com.project.apsas.dto.event.SubmitCodeEvent;
import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.request.CreateSubmissionRequest;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.dto.response.CreateSubmissionResponse;
import com.project.apsas.dto.response.SubmissionResponse;
import com.project.apsas.dto.response.submission.SubmissionDetailResponse;
import com.project.apsas.dto.response.submission.SubmissionItem;
import com.project.apsas.dto.response.submission.SubmittedAssigmentResponse;
import com.project.apsas.entity.*;
import com.project.apsas.enums.EvaluationVisibility;
import com.project.apsas.enums.StatusSubmission;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.integration.kafka.ai.KafkaFeedbackProvider;
import com.project.apsas.integration.kafka.jubge.KafkaRCEProducer;
import com.project.apsas.mapper.SubmissionMapper;
import com.project.apsas.repository.*;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.NotificationService;
import com.project.apsas.service.SubmissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
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
    
    NotificationService notificationService;

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





    private SubmissionResponse mapToResponse(Submission submission) {
        String codePreview = null;
        Integer codeLength = null;

        if (submission.getCode() != null) {
            codeLength = submission.getCode().length();
            codePreview = submission.getCode().substring(0, Math.min(500, codeLength));
        }

        // Kiểm tra null để tránh NPE
        Assignment assignment = submission.getAssignment();
        User user = submission.getUser();
        
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(assignment != null ? assignment.getId() : submission.getAssignmentId())
                .assignmentTitle(assignment != null ? assignment.getTitle() : null)
                .studentId(submission.getUserId())
                .studentName(user != null ? user.getName() : null)
                .studentEmail(user != null ? user.getEmail() : null)
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
        // Validate request
        if (req == null || req.getCode() == null || req.getCode().trim().isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        }
        if (req.getAssignmentId() == null || req.getCourseId() == null || req.getLanguageId() <= 0) {
            throw new AppException(ErrorCode.VALIDATION_FAILED);
        }
        
        // Get current user ID
        String currentId = authService.currentId();
        if (currentId == null || currentId.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        Long userId;
        try {
            userId = Long.parseLong(currentId);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        Long assignmentId = req.getAssignmentId();
        Long courseId = req.getCourseId();

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
        if(user.getProgress() != null) {
            ProgressSkill progressSkill = ProgressSkill.builder()
                    .skillId(assignment.getSkill().getId())
                    .progressId(user.getProgress().getId())
                    .level(1)
                    .score(BigDecimal.valueOf(0))
                    .build();
            progressSkillRepository.save(progressSkill);
        }
        // Kiểm tra assignment có evaluations không
        if(assignment.getAssignmentEvaluations() == null || assignment.getAssignmentEvaluations().isEmpty())
            throw new AppException(ErrorCode.BAD_REQUEST);
        
        AssignmentEvaluation evaluation = assignment.getAssignmentEvaluations().stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));
        
        FeedbackEvent feedbackEvent = FeedbackEvent.builder()
                .submissionId(submission.getId())
                .code(req.getCode())
                .statement_md(assignment.getStatementMd())
                .language(submission.getLanguage())
                .build();
        SubmitCodeEvent submitCodeEvent = SubmitCodeEvent.builder()
                .code(req.getCode())
                .submissionId(submission.getId())
                .configJson(evaluation.getConfigJson())
                .languageId(Integer.parseInt(submission.getLanguage()))
                .build();
        kafkaRCEProducer.push(executeTopic,submitCodeEvent.getSubmissionId().toString(),submitCodeEvent);

        kafkaFeedbackProvider.push(feedbackTopic,submitCodeEvent.getSubmissionId().toString(),feedbackEvent);

        // Create notification for teacher
        try {
            notificationService.createSubmissionNotification(
                    courseId,
                    assignmentId,
                    userId,
                    user.getName(),
                    assignment.getTitle()
            );
            log.info("Created submission notification for user {} on assignment {}", userId, assignmentId);
        } catch (Exception e) {
            log.error("Failed to create submission notification for user {} on assignment {}", 
                    userId, assignmentId, e);
        }

        return CreateSubmissionResponse.builder()
                .submissionId(submission.getId())
                .build();
    }

    /**
     * Lấy tất cả submissions của một student trong course
     * Bao gồm cả assignments chưa nộp
     */
    @Override
    public SubmittedAssigmentResponse getSubmissionHistory( Long courseId, Long assignmentId, Long studentId) {
        long userId = 0;
        if(studentId == null)
             userId = Long.parseLong(authService.currentId());
        else
            userId = studentId;
        if(courseId == null || assignmentId == null) throw new AppException(ErrorCode.BAD_REQUEST);
        // 1. Lấy danh sách Entity từ DB
        List<Submission> submissions = submissionRepository
                .findByUserIdAndCourseIdAndAssignmentIdOrderByAttemptNoDesc(userId, courseId, assignmentId);

        // 2. Map từ Entity sang DTO (SubmissionItem)
        List<SubmissionItem> submissionItems = submissions.stream()
                .map(this::mapToSubmissionItem)
                .collect(Collectors.toList());

        // 3. Trả về Response bọc bên ngoài
        return SubmittedAssigmentResponse.builder()
                .items(submissionItems)
                .build();
    }

    // Helper method để map data (Có thể dùng MapStruct thay thế nếu muốn)
    private SubmissionItem mapToSubmissionItem(Submission submission) {
        return SubmissionItem.builder()
                .id(submission.getId())
                .passed(submission.getPassed() != null ? submission.getPassed() : false)
                .status(submission.getStatus() != null ? submission.getStatus() : StatusSubmission.FAILED)
                .score(submission.getScore())
                .attemptNo(submission.getAttemptNo())
                .language(submission.getLanguage())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
    @Override
    public SubmissionDetailResponse getSubmissionDetailForStudent(Long submissionId) {
        // A. LẤY USER HIỆN TẠI (Từ Security Context)
        Long userId = Long.parseLong(authService.currentId());

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 1. Lấy Submission (Kèm Assignment)
        Submission submission = submissionRepository.findByIdWithAssignment(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

        // B. KIỂM TRA QUYỀN SỞ HỮU (Quan trọng nhất)
        // Nếu người đang login KHÔNG PHẢI là chủ nhân bài nộp -> Chặn ngay
        if (!submission.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN); // Hoặc ACCESS_DENIED
        }

        // C. (Optional) Kiểm tra xem User còn trong khóa học không?
        // Thường thì bước B là đủ. Nhưng nếu bạn muốn chắc chắn sinh viên chưa bị kick khỏi khóa học:
        // boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUser.getId(), submission.getCourse().getId());
        // if (!isEnrolled) throw new AppException(ErrorCode.NOT_ENROLLED);

        Assignment assignment = submission.getAssignment();

        // 2. Xử lý Report JSON (Lấy kết quả chạy thực tế)
        List<TestCaseResult> publicTestCaseResults = getTestCaseResultsFromReport(submission);

        // 3. Map sang Response DTO
        return SubmissionDetailResponse.builder()
                .id(submission.getId())
                .title(assignment.getTitle())
                .statementMd(assignment.getStatementMd())
                .language(submission.getLanguage())
                .code(submission.getCode())
                .status(submission.getStatus())
                .suggestion(submission.getSuggestion())
                .bigOComplexityTime(submission.getBigOComplexityTime())
                .bigOComplexitySpace(submission.getBigOComplexitySpace())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .passed(submission.getPassed())
                .attemptNo(submission.getAttemptNo())
                .feedbackTeachers(submission.getFeedbacks())
                .testCases(publicTestCaseResults)
                .build();
    }

    /**
     * Hàm helper: Parse reportJson từ Submission và lọc ra các test case PUBLIC
     */
    private List<TestCaseResult> getTestCaseResultsFromReport(Submission submission) {
        String json = submission.getReportJson();

        // Nếu chưa có report (đang chấm hoặc lỗi hệ thống) thì trả về rỗng
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        try {
            // 1. Parse JSON thành Object ReportCongfigSubmission
            ReportCongfigSubmission report = objectMapper.readValue(json, ReportCongfigSubmission.class);

            if (report.getTestCases() != null) {
                // 2. FILTER: Chỉ trả về kết quả của các test case PUBLIC cho sinh viên xem
                // (Ẩn các test case HIDDEN để tránh lộ đề)
                return report.getTestCases().stream()
                        .filter(tc -> EvaluationVisibility.PUBLIC.equals(tc.getVisibility()))
                        .collect(Collectors.toList());
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing reportJson for submission {}", submission.getId(), e);
        }

        return Collections.emptyList();
    }
    @Override
    public SubmissionDetailResponse getSubmissionDetailForTeacher(Long submissionId) {
        // A. LẤY USER HIỆN TẠI (Giảng viên đang login)
        Long userId = Long.parseLong(authService.currentId());

        User currentTeacher = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 1. Lấy Submission (Kèm Assignment)
        Submission submission = submissionRepository.findByIdWithAssignment(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_FOUND));

        // B. KIỂM TRA QUYỀN SỞ HỮU KHÓA HỌC
        // Logic: Bài nộp thuộc khóa học A -> Khóa học A do ai tạo? -> Có phải là người đang login không?
        Course course = submission.getCourse();

        // Lưu ý: Đảm bảo Entity Course của bạn có quan hệ với User qua field 'creator' hoặc 'createdBy'
        // Nếu dùng ID thuần: if (!course.getCreatedBy().equals(currentTeacher.getId()))
        if (!course.getCreator().getId().equals(currentTeacher.getId())) {
            // Nếu không phải người tạo khóa học -> Chặn
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Assignment assignment = submission.getAssignment();

        // 2. Xử lý Report JSON: LẤY TẤT CẢ (Không lọc Public - Teacher được xem hết)
        List<TestCaseResult> allTestCaseResults = getAllTestCaseResultsFromReport(submission);

        // 3. Map sang Response DTO
        return SubmissionDetailResponse.builder()
                .id(submission.getId())
                .title(assignment.getTitle())
                .statementMd(assignment.getStatementMd())
                .language(submission.getLanguage())
                .code(submission.getCode())
                .status(submission.getStatus())
                .suggestion(submission.getSuggestion())
                .bigOComplexityTime(submission.getBigOComplexityTime())
                .bigOComplexitySpace(submission.getBigOComplexitySpace())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .passed(submission.getPassed())
                .attemptNo(submission.getAttemptNo())
                .feedbackTeachers(submission.getFeedbacks())
                .testCases(allTestCaseResults)
                .build();
    }

    /**
     * Helper method: Parse reportJson và trả về TẤT CẢ test case (Dành cho Teacher)
     */
    private List<TestCaseResult> getAllTestCaseResultsFromReport(Submission submission) {
        String json = submission.getReportJson();

        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        try {
            // Parse JSON thành Object Report
            ReportCongfigSubmission report = objectMapper.readValue(json, ReportCongfigSubmission.class);

            if (report.getTestCases() != null) {
                // KHÁC BIỆT: Trả về nguyên list, KHÔNG FILTER
                return report.getTestCases();
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing reportJson for submission {}", submission.getId(), e);
        }

        return Collections.emptyList();
    }
}
