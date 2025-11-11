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
            attempts = "3", // Chạy 1 lần, retry 2 lần (tổng 3)
            backoff = @Backoff(delay = 1000), // Mỗi lần retry cách nhau 1 giây
            autoCreateTopics = "true", // Tự tạo topic DLQ nếu chưa có
            dltStrategy = DltStrategy.FAIL_ON_ERROR, // Chiến lược khi vào DLq
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "${message-queue.topic.feedback.name}", groupId = "group_ai")
    public void receiveFeedback(ConsumerRecord<String, FeedbackEvent> record, Acknowledgment ack) {
        try {
            log.info("▶ processing {}-{}@{} key={} payload={}",
                    record.topic(), record.partition(), record.offset(), record.key(), record.value());
            FeedbackEvent event = record.value();
            submissionRepository.findById(event.getSubmissionId())
                    .orElseThrow(() -> new RuntimeException("không có bài nôp hợp lệ"));
            CodeFeedbackDTO result =  aiFeedbackService.reviewAsync(
                            event.getCode(),
                            event.getLanguage(),
                            event.getStatement_md())
                    .join();
            submissionService.updataFeedbackByAI(event.getSubmissionId(), result);
            ack.acknowledge();
            log.info("✔ done {}-{}@{} key={}", record.topic(), record.partition(), record.offset(), record.key());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
