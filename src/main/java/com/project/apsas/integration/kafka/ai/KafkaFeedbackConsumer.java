package com.project.apsas.integration.kafka.ai;

import com.project.apsas.dto.event.FeedbackEvent;
import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.AIFeedbackService;
import com.project.apsas.service.SubmissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaFeedbackConsumer {
    AIFeedbackService aiFeedbackService;
    SubmissionRepository  submissionRepository;
    SubmissionService submissionService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000),
            autoCreateTopics = "true",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "${message-queue.topic.feedback.name}", groupId = "group_ai")
    public void receiveFeedback(
            ConsumerRecord<String, FeedbackEvent> record,
            Acknowledgment ack,
            @Header(name = KafkaHeaders.DELIVERY_ATTEMPT, required = false, defaultValue = "1") int deliveryAttempt
    ) {
        final int TOTAL_ATTEMPTS = 3;
        FeedbackEvent event = record.value(); // Lấy event ra sớm để dùng trong catch

        try {
            log.info("▶ processing {}-{}@{} key={} payload={} (Attempt {}/{})",
                    record.topic(), record.partition(), record.offset(), record.key(), record.value(),
                    deliveryAttempt, TOTAL_ATTEMPTS);

            submissionRepository.findById(event.getSubmissionId())
                    .orElseThrow(() -> new RuntimeException("không có bài nôp hợp lệ"));

            // Gọi AI
            CodeFeedbackDTO result =  aiFeedbackService.reviewAsync(
                            event.getCode(),
                            event.getLanguage(),
                            event.getStatement_md())
                    .join();

            // --- SUCCESS PATH (ĐƯỜNG THÀNH CÔNG) ---
            // Nếu AI call thành công:
            // 1. Cập nhật với 'result'
            // 2. Truyền 'false' vì đã thành công, không cần retry
            submissionService.updataFeedbackByAI(
                    event.getSubmissionId(),
                    result,
                    false // <-- Thành công, không retry
            );

            // 3. Commit message
            ack.acknowledge();
            log.info("✔ done {}-{}@{} key={}", record.topic(), record.partition(), record.offset(), record.key());

        } catch (Exception e) {
            log.warn("⚠ failed processing {}-{}@{} key={} (Attempt {}/{})",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    deliveryAttempt, TOTAL_ATTEMPTS, e);

            // --- FAILURE PATH (ĐƯỜNG THẤT BẠI) ---
            // Đây là logic bạn yêu cầu:

            // 1. Tính toán flag: 'true' cho lần 1, 2. 'false' cho lần 3 (lần cuối).
            boolean shouldRetryOnFailure = (deliveryAttempt < TOTAL_ATTEMPTS);

            try {
                // 2. Gọi hàm update với 'result' là null và flag đã tính
                // Hàm này sẽ:
                // - Lần 1, 2: set true (chờ retry)
                // - Lần 3: set false ("đã hết thử inset dù kh có j")
                submissionService.updataFeedbackByAI(
                        event.getSubmissionId(),
                        null, // <-- result là null vì AI call thất bại
                        shouldRetryOnFailure // <-- 'true' (lần 1,2) hoặc 'false' (lần 3)
                );
            } catch (Exception updateEx) {
                // Lỗi khi đang cố cập nhật trạng thái lỗi
                log.error("!! Failed to update submission failure status for {}. Error: {}",
                        event.getSubmissionId(), updateEx.getMessage());
            }

            // 3. Ném lỗi gốc để kích hoạt retry (hoặc gửi tới DLT)
            throw new RuntimeException(e);
        }
    }


}
