package com.project.apsas.integration.kafka.jubge;

import com.project.apsas.dto.event.FeedbackEvent;

import com.project.apsas.dto.event.SubmitCodeEvent;
import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.request.SubmissionRequestDto;
import com.project.apsas.repository.SubmissionRepository;
import com.project.apsas.service.RCEService;
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
public class KafkaRCEConsumer {

    SubmissionRepository submissionRepository;
    SubmissionService submissionService;
    RCEService  rceService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000),
            autoCreateTopics = "true",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            dltTopicSuffix = ".DLT"
    )
        @KafkaListener(topics = "${message-queue.topic.execute.name}", groupId = "group_execute")
    public void receiveSubmitcode(
            ConsumerRecord<String, SubmitCodeEvent> record,
            Acknowledgment ack,
            @Header(name = KafkaHeaders.DELIVERY_ATTEMPT, required = false, defaultValue = "1") int deliveryAttempt
    ) {
        final int TOTAL_ATTEMPTS = 3;
        SubmitCodeEvent event = record.value();

        try {
            log.info("▶ processing RCE {}-{}@{} key={} (Attempt {}/{})",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    deliveryAttempt, TOTAL_ATTEMPTS);

            // 1. Map từ Event (Kafka) sang DTO (Service)
            // (Giả sử SubmitCodeEvent chứa các trường này)
            SubmissionRequestDto requestDto = new SubmissionRequestDto();
            requestDto.setCode(event.getCode());
            requestDto.setLanguageId(event.getLanguageId());
            requestDto.setConfigJson(event.getConfigJson());

            // 2. Gọi RCE service (Judge0) để chạy code
            ReportCongfigSubmission report = rceService.evaluateCode(requestDto);

            // --- SUCCESS PATH (ĐƯỜNG THÀNH CÔNG) ---
            // 3. Cập nhật submission với report đầy đủ.
            // Truyền 'false' vì đã thành công, không cần retry.
            submissionService.updataReportConfig(
                    event.getSubmissionId(),
                    report,
                    false // <-- false = không retry
            );

            // 4. Commit message
            ack.acknowledge();
            log.info("✔ done RCE {}-{}@{} key={}", record.topic(), record.partition(), record.offset(), record.key());

        } catch (Exception e) {
            log.warn("⚠ failed RCE {}-{}@{} key={} (Attempt {}/{})",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    deliveryAttempt, TOTAL_ATTEMPTS, e);

            // --- FAILURE PATH (ĐƯỜNG THẤT BẠI) ---
            // 1. Tính toán flag retry
            // 'true' cho lần 1, 2. 'false' cho lần 3 (lần cuối).
            boolean shouldRetryOnFailure = (deliveryAttempt < TOTAL_ATTEMPTS);

            try {
                // 2. Cập nhật submission với report=null
                // và flag 'shouldRetryOnFailure'
                submissionService.updataReportConfig(
                        event.getSubmissionId(),
                        null, // <-- null report vì thất bại
                        shouldRetryOnFailure // <-- 'true' (lần 1,2) hoặc 'false' (lần 3)
                );
            } catch (Exception updateEx) {
                log.error("!! Failed to update RCE failure status for {}. Error: {}",
                        event.getSubmissionId(), updateEx.getMessage());
            }

            // 3. Ném lỗi gốc để kích hoạt retry (hoặc gửi tới DLT)
            throw new RuntimeException(e);
        }
    }
}
