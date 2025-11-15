package com.project.apsas.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.mapping.ConfigJson;
import com.project.apsas.dto.mapping.TestCase;
import com.project.apsas.dto.request.assignment.CreateAssigmentRequest;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.assignment.TestCaseConfig;
import com.project.apsas.entity.Assignment;
import com.project.apsas.entity.AssignmentEvaluation;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.mapper.AssignmentMapper;
import com.project.apsas.repository.AssignmentRepository;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.service.AssignmentService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AssignmentServiceImpl implements AssignmentService {


    // Inject 2 repository cần thiết
    AssignmentRepository assignmentRepository;
    TutorialRepository tutorialRepository;
    AssignmentMapper assignmentMapper;
    ObjectMapper objectMapper;

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

}
