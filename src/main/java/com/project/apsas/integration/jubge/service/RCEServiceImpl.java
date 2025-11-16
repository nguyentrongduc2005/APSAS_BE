package com.project.apsas.integration.jubge.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.mapping.ConfigJson;
import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.mapping.TestCase;
import com.project.apsas.dto.mapping.TestCaseResult;
import com.project.apsas.dto.request.SubmissionRequestDto;
import com.project.apsas.integration.jubge.JubgeApiClient;
import com.project.apsas.integration.jubge.dto.*;
import com.project.apsas.service.RCEService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RCEServiceImpl implements RCEService {

    ObjectMapper objectMapper;

    JubgeApiClient jubgeApiClient;

    @Override
    public ReportCongfigSubmission evaluateCode(SubmissionRequestDto request) throws Exception {

        ConfigJson configJson = objectMapper.readValue(request.getConfigJson(), ConfigJson.class);

        List<TestCase> testCases = configJson.getTestCases();
        List<SubmissionRCERequest> list = testCases.stream().map(testCase ->
                {
                   return SubmissionRCERequest.builder()
                            .stdin(testCase.getIn())
                            .sourceCode(request.getCode())
                            .languageId(request.getLanguageId())
                            .expectedOutput(testCase.getOut())
                            .build();
                }
                ).collect(Collectors.toList());
        BatchSubmissionRequest batchSubmissionRequest = BatchSubmissionRequest.builder()
                .submissions(list)
                .build();
        BatchSubmissionResult result = executeCodeWithTestCases(batchSubmissionRequest);

        List<SubmissionRCEResponse> judgeResults = result.getSubmissions();

        // 2. Chuẩn bị các biến tổng hợp và danh sách DTO output
        List<TestCaseResult> reportTestCases = new ArrayList<>();
        double totalTime = 0;
        long totalMemory = 0; // Dùng long để cộng cho an toàn, tránh tràn số
        int passCount = 0;
        int totalCases = judgeResults.size();


        for (int i = 0; i < totalCases; i++) {
            // Lấy kết quả từ Judge0
            SubmissionRCEResponse judgeResult = judgeResults.get(i);
            // Lấy config gốc (để lấy stdin và visibility)
            TestCase originalCase = testCases.get(i);

            String statusDesc = judgeResult.getStatus().getDescription();
            double time = Double.parseDouble(judgeResult.getTime());
            int memory = judgeResult.getMemory();

            // Cập nhật biến tổng
            if ("Accepted".equals(statusDesc)) {
                passCount++;
            }
            totalTime += time;
            totalMemory += memory;

            // Xây dựng DTO 'TestCaseResult' (cho report)
            TestCaseResult tcResult = TestCaseResult.builder()
                    .status(statusDesc)
                    .time(time)
                    .memory(memory)
                    .visibility(originalCase.getVisibility()) // Lấy từ config gốc
                    .stdin(originalCase.getIn())             // Lấy từ config gốc
                    .stdout(judgeResult.getStdout())         // Lấy từ Judge0
                    .expectedOutput(originalCase.getOut()) // Lấy từ config gốc
                    .build();

            reportTestCases.add(tcResult);
        }

        // 4. Tính toán trung bình (tránh chia cho 0)
        double averageTime = (totalCases > 0) ? totalTime / totalCases : 0;
        double averageMemory = (totalCases > 0) ? (double) totalMemory / totalCases : 0;

        // 5. Xây dựng và trả về ReportCongfigSubmission cuối cùng
        return ReportCongfigSubmission.builder()
                .averageTime(averageTime)
                .averageMemory(averageMemory)
                .totalTestCases(totalCases)
                .passedTestCases(passCount)
                .testCases(reportTestCases)
                .build();
    }

    private BatchSubmissionResult executeCodeWithTestCases(BatchSubmissionRequest batchSubmissionRequest) throws Exception {


        List<SubmissionToken> tokens = jubgeApiClient.createBatchSubmissions(batchSubmissionRequest, false);

        // Nối các token lại (vd: "token1,token2,token3")
        String tokenString = tokens.stream()
                .map(SubmissionToken::getToken)
                .collect(Collectors.joining(","));

        BatchSubmissionResult finalResult = null;
        boolean allDone = false;
        while (!allDone) {
            // Đợi 1 giây (hoặc 500ms) trước khi kiểm tra lại
            Thread.sleep(1000);

            // Lấy kết quả của TẤT CẢ token
            finalResult = jubgeApiClient.getBatchSubmissionResults(
                    tokenString,
                    false,
                    "stdout,stderr,message,time,memory,status,token,compile_output"); // Thêm fields

            // Kiểm tra xem có CÒN test case nào đang chạy (status 1 hoặc 2) không
            allDone = finalResult.getSubmissions().stream()
                    .noneMatch(SubmissionRCEResponse::isProcessing);

        }
        return finalResult;

    }
}
