package com.project.apsas.service.impl;

import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.service.AIFeedbackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AIFeedbackServiceImpl implements AIFeedbackService {


    private final ChatClient chatClient;
    private final String systemPromptText;
    private final String userPromptText;
    private final AuthServiceImpl authServiceImpl;

    public AIFeedbackServiceImpl(ChatClient.Builder chatClient, AuthServiceImpl authServiceImpl) {
        this.chatClient = chatClient.build();

        this.systemPromptText = """
                Bạn là một trợ giảng AI chấm code.
                    Nhiệm vụ của bạn là phân tích code và trả về JSON.
                
                    Hãy làm theo CÁC BƯỚC SAU:
                
                    **BƯỚC 1: Phân tích Code**
                    Đọc kỹ mô tả bài toán và code của sinh viên.
                    Phân tích tính đúng đắn logic, hiệu suất (Time/Space), và code style.
                
                    **BƯỚC 2: Viết Feedback (feedback)**
                    Viết nhận xét chi tiết vào trường "feedback".\s
                    Nếu code chạy đúng logic (ngay cả khi chậm), HÃY NÓI RÕ LÀ CODE ĐÚNG LOGIC.
                
                    **BƯỚC 3: Viết Gợi ý (suggestion)**
                    Viết gợi ý cải thiện vào trường "suggestion".\s
                    Nếu code O(n^2) và có thể tối ưu O(n), hãy gợi ý ở đây.
                
                    **BƯỚC 4: Viết Độ phức tạp (bigOComplexityTime, bigOComplexitySpace)**
                    Điền độ phức tạp Time và Space.
                                                                                                                                                                               5.  **Gợi ý (suggestion):** Gợi ý cách cải thiện code, đặc biệt nếu có giải pháp tối ưu hơn.
            """;

        // 2. USER PROMPT: Định nghĩa dữ liệu đầu vào
        this.userPromptText = """
            Hãy review code dựa trên thông tin sau:

            **Mô tả bài toán (Markdown):**
            {statement}

            **Ngôn ngữ lập trình:**
            {language}

            **Code của sinh viên:**
            ```{language}
            {code}
            ```
            """;
        this.authServiceImpl = authServiceImpl;
    }



    @Override
    public CodeFeedbackDTO review( String code, String language, String statement_md) {

        PromptTemplate userTemplate = new PromptTemplate(this.userPromptText);
        Prompt filledUserText = userTemplate.create(Map.of(
                "statement", statement_md,
                "language", language,
                "code", code
        ));


        return chatClient
                .prompt(filledUserText)
                .system(this.systemPromptText)
                .call()
                .entity(CodeFeedbackDTO.class);
    }

    @Override
    public CompletableFuture<CodeFeedbackDTO> reviewAsync(String code, String language, String statement_md) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (code == null) throw new NullPointerException("code");
                if (statement_md == null) throw new NullPointerException("language");
                CodeFeedbackDTO result = review(code, language, statement_md);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
    }
}
