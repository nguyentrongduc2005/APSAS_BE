package com.project.apsas.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.mapping.ConfigJson;
import com.project.apsas.dto.mapping.TestCase;
import com.project.apsas.dto.response.assignment.AssignmentDetailDTO;
import com.project.apsas.dto.request.assignment.AssignmentListItemDTO;
import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.request.assignment.UpdateAssignmentRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.assignment.TestCaseConfig;
import com.project.apsas.dto.response.tutorial.DetailAssignmentResponse;
import com.project.apsas.entity.*;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.mapper.AssignmentMapper;
import com.project.apsas.repository.AssignmentRepository;
import com.project.apsas.repository.CourseAssignmentRepository;
import com.project.apsas.repository.SkillRepository;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.service.AssignmentService;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.NotificationService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {


    // Inject 2 repository cần thiết
    AssignmentRepository assignmentRepository;
    TutorialRepository tutorialRepository;
    AssignmentMapper assignmentMapper;
    ObjectMapper objectMapper;
    SkillRepository skillRepository;
    AuthService authService;
    CourseAssignmentRepository courseAssignmentRepository;
    NotificationService notificationService;
    Parser markdownParser;
    HtmlRenderer htmlRenderer;

    @Override
    @Transactional
    public CreateAssignmentResponse createAssignment(Long tutorialId, CreateAssigmentRequest request) throws JsonProcessingException {

        // 1. Kiểm tra Tutorial
        tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // 2. Build Assignment (cha)
        Assignment newAssignment = Assignment.builder()
                .tutorialId(tutorialId)
                .skillId(request.getSkillId())
                .title(request.getTitle())
                .statementMd(request.getStatementMd())
                .maxScore(request.getMaxScore())
                .attemptsLimit(request.getAttemptsLimit())
                .proficiency(request.getProficiency())
                .orderNo(request.getOrderNo())
                .assignmentEvaluations(new HashSet<>())
                .build();

        // 3. Build Evaluations (con)
        if (request.getEvaluations() != null) {
            for (var evalRequest : request.getEvaluations()) {
                AssignmentEvaluation evalEntity = AssignmentEvaluation.builder()
                        .name(evalRequest.getName())
                        .type(evalRequest.getType())
                        .configJson(evalRequest.getConfigJson())
                        .assignment(newAssignment)
                        .build();
                newAssignment.getAssignmentEvaluations().add(evalEntity);
            }
        }

        // 4. Lưu vào CSDL
        Assignment savedAssignment = assignmentRepository.save(newAssignment);

        savedAssignment.setSkill(skillRepository.findById(savedAssignment.getSkillId()).get());
        // 5. Map sang Response DTO (bằng MapStruct)
        CreateAssignmentResponse res = assignmentMapper.toCreateResponse(savedAssignment);

        // --- PHẦN SỬA LỖI ---

        // 6. Xử lý logic test case config một cách an toàn

        // SỬA LỖI 1: Phải kiểm tra 'Optional' trước khi '.get()'
        var firstEvaluationOpt = savedAssignment.getAssignmentEvaluations().stream().findFirst();

        List<TestCaseConfig> finalTestCaseConfigs = new ArrayList<>(); // Khởi tạo rỗng

        if (firstEvaluationOpt.isPresent()) {
            String configJson = firstEvaluationOpt.get().getConfigJson();
            ConfigJson configJsonObject = objectMapper.readValue(configJson, ConfigJson.class);

            // SỬA LỖI 2: Phải kiểm tra 'getTestCases()' có null không
            List<TestCase> testCasesFromConfig = configJsonObject.getTestCases();

            if (testCasesFromConfig != null) { // <-- KIỂM TRA NULL Ở ĐÂY
                List<TestCase> testCasesPublic = testCasesFromConfig.stream()
                        .filter(testCase -> "PUBLIC".equals(testCase.getVisibility().name()))
                        .toList();

                finalTestCaseConfigs = assignmentMapper.toTestCaseConfigs(testCasesPublic);
            }
        }

        // 7. Gán danh sách (có thể rỗng) vào response
        res.setTestCaseConfigs(finalTestCaseConfigs);

        return res;
    }

    /**
     * Phương thức helper để chuyển đổi từ Entity sang Response DTO
     */

    @Override
    public CreateAssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request)
            throws JsonProcessingException {
        // 1. Lấy Assignment (cha) và Tutorial
        Assignment existingAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_EXISTED));

        Tutorial tutorial = tutorialRepository.findById(existingAssignment.getTutorialId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // 2. CHECK BẢO MẬT (Giả sử Tutorial có `createdBy`)
        Long currentUsername = Long.parseLong(authService.currentId());
        if (!tutorial.getCreatedBy().equals(currentUsername)) {
            throw new AppException(ErrorCode.FORBIDDEN); // Không có quyền
        }

        // 3. Cập nhật các trường của Assignment (cha)
        // (Dùng MapStruct hoặc thủ công)
        existingAssignment.setSkillId(request.getSkillId());
        existingAssignment.setTitle(request.getTitle());
        existingAssignment.setStatementMd(request.getStatementMd());
        existingAssignment.setMaxScore(request.getMaxScore());
        existingAssignment.setAttemptsLimit(request.getAttemptsLimit());
        existingAssignment.setProficiency(request.getProficiency());
        existingAssignment.setOrderNo(request.getOrderNo());

        // 4. XỬ LÝ ĐỒNG BỘ EVALUATIONS (CON)
        // (Đây là logic quan trọng nhất)

        // Lấy map (id -> entity) của các 'con' hiện có
        Map<Long, AssignmentEvaluation> existingEvalsMap = existingAssignment
                .getAssignmentEvaluations().stream()
                .collect(Collectors.toMap(AssignmentEvaluation::getId, eval -> eval));

        // Tạo một Set (danh sách) 'con' mới
        Set<AssignmentEvaluation> newEvaluationsSet = new HashSet<>();

        if (request.getEvaluations() != null) {
            for (var evalRequest : request.getEvaluations()) {

                if (evalRequest.getId() == null) {
                    // TRƯỜNG HỢP 1: THÊM MỚI (id = null)
                    AssignmentEvaluation newEval = AssignmentEvaluation.builder()
                            .name(evalRequest.getName())
                            .type(evalRequest.getType())
                            .configJson(evalRequest.getConfigJson())
                            .assignment(existingAssignment) // Gán 'cha'
                            .build();
                    newEvaluationsSet.add(newEval);

                } else {
                    // TRƯỜNG HỢP 2: CẬP NHẬT (id != null)
                    AssignmentEvaluation existingEval = existingEvalsMap.get(evalRequest.getId());
                    if (existingEval != null) {
                        // Lấy 'con' từ Map và cập nhật
                        existingEval.setName(evalRequest.getName());
                        existingEval.setType(evalRequest.getType());
                        existingEval.setConfigJson(evalRequest.getConfigJson());
                        newEvaluationsSet.add(existingEval);
                    }
                    // (Nếu existingEval == null -> client gửi ID bậy, ta bỏ qua)
                }
            }
        }

        // TRƯỜNG HỢP 3: XÓA
        // Nhờ có 'orphanRemoval=true', ta chỉ cần xóa 'con' khỏi 'cha'
        // và 'con' sẽ tự động bị xóa khỏi DB.
        existingAssignment.getAssignmentEvaluations().clear();
        existingAssignment.getAssignmentEvaluations().addAll(newEvaluationsSet);

        // 5. Lưu (cha) vào CSDL
        // (Nhờ @Transactional và CascadeType.ALL, Hibernate sẽ tự động
        // tìm, thêm, sửa, xóa các 'con' trong 'newEvaluationsSet')
        Assignment savedAssignment = assignmentRepository.save(existingAssignment);



        // 6. Xử lý logic Test Case Config (Giống hệt hàm create)
        // (Copy y chang logic từ hàm 'createAssignment' của bạn)

        CreateAssignmentResponse res = assignmentMapper.toCreateResponse(savedAssignment);
        var firstEvaluationOpt = savedAssignment.getAssignmentEvaluations().stream().findFirst();
        List<TestCaseConfig> finalTestCaseConfigs = new ArrayList<>();

        if (firstEvaluationOpt.isPresent()) {
            String configJson = firstEvaluationOpt.get().getConfigJson();
            ConfigJson configJsonObject = objectMapper.readValue(configJson, ConfigJson.class);
            List<TestCase> testCasesFromConfig = configJsonObject.getTestCases();

            if (testCasesFromConfig != null) {
                List<TestCase> testCasesPublic = testCasesFromConfig.stream()
                        .filter(testCase -> "PUBLIC".equals(testCase.getVisibility().name()))
                        .toList();
                finalTestCaseConfigs = assignmentMapper.toTestCaseConfigs(testCasesPublic);
            }
        }
        res.setTestCaseConfigs(finalTestCaseConfigs);

        return res;
    }

    @Override
    public void setTime(Long assignmentId, Long courseId, LocalDateTime openAt, LocalDateTime dueAt) {
        if(openAt == null || dueAt == null) throw new AppException(ErrorCode.BAD_REQUEST);

        if(!dueAt.isAfter(openAt)) throw new AppException(ErrorCode.TIME_INVALID);


        if(dueAt.isBefore(LocalDateTime.now())) throw new AppException(ErrorCode.TIME_INVALID);
        Long userId = Long.parseLong(authService.currentId());
        CourseAssignment courseAssignment = courseAssignmentRepository.findById(
                CourseAssignment.PK.builder()
                        .courseId(courseId)
                        .assignmentId(assignmentId)
                .build()).orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_EXISTED));
        Course course = courseAssignment.getCourse();
        if(!course.getCreator().getId().equals(userId)) throw new AppException(ErrorCode.FORBIDDEN);
        courseAssignment.setOpenAt(openAt);
        courseAssignment.setDueAt(dueAt);

        courseAssignmentRepository.save(courseAssignment);

        // Create notification for students
        Assignment assignment = courseAssignment.getAssignment();
        String message = String.format("Bài tập '%s' đã được mở. Hạn nộp: %s",
                assignment.getTitle(),
                dueAt.toString());

        try {
            notificationService.createAssignmentNotification(courseId, assignment.getTitle(), message);
            log.info("Created assignment notifications for course {} and assignment {}", courseId, assignmentId);
        } catch (Exception e) {
            log.error("Failed to create assignment notifications", e);
        }
    }

    @Override
    public List<AssignmentListItemDTO> getAssignmentsByCourseId(Long courseId) {
        log.info("Getting assignments for course ID: {}", courseId);

        List<AssignmentListItemDTO> assignments = courseAssignmentRepository
                .findAssignmentsByCourseId(courseId);

        log.info("Found {} assignments for course ID: {}", assignments.size(), courseId);
        return assignments;
    }


    @Override
    public AssignmentDetailDTO getAssignmentDetail(Long courseId, Long assignmentId) {
        log.info("Getting assignment detail for courseId: {} and assignmentId: {}", courseId, assignmentId);

        AssignmentDetailDTO assignmentDetail = courseAssignmentRepository
                .findAssignmentDetailByCourseIdAndAssignmentId(courseId, assignmentId)
                .orElseThrow(()  -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        log.info("Found assignment detail: {}", assignmentDetail.getTitle());
        return assignmentDetail;
    }

    @Override
    public DetailAssignmentResponse detailAssignmentTutorial(Long assignmentId) throws JsonProcessingException {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_EXISTED));

        String markdownInput = assignment.getStatementMd();

        String htmlOutput = "";
        if (markdownInput != null) {
            Node document = markdownParser.parse(markdownInput);
            htmlOutput = htmlRenderer.render(document);
        }

        // --- SỬA ĐOẠN NÀY ---

        // 1. Tìm evaluation, nếu không có thì trả về null (không ném lỗi nữa)
        AssignmentEvaluation assignmentEvaluation = assignment.getAssignmentEvaluations().stream()
                .findFirst()
                .orElse(null);

        List<TestCase> testCases = null; // Mặc định là null

        // 2. Chỉ parse JSON nếu evaluation tồn tại
        if (assignmentEvaluation != null) {
            String configJsonString = assignmentEvaluation.getConfigJson();
            if (configJsonString != null && !configJsonString.isEmpty()) {
                // Bọc try-catch đề phòng JSON lỗi format cũng không làm sập API
                try {
                    ConfigJson configJson = objectMapper.readValue(configJsonString, ConfigJson.class);
                    testCases = configJson.getTestCases();
                } catch (Exception e) {
                    // Log lỗi nếu cần, hoặc bỏ qua
                    System.err.println("Lỗi parse JSON config: " + e.getMessage());
                }
            }
        }

        return DetailAssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .attemptsLimit(assignment.getAttemptsLimit())
                .createdDate(assignment.getCreatedAt())
                .proficiency(assignment.getProficiency())
                .statementHtml(htmlOutput)
                .testCases(testCases) // Giá trị này sẽ là null nếu không có evaluation
                .maxScore(assignment.getMaxScore())
                .orderNo(assignment.getOrderNo())
                .build();
    }
}
