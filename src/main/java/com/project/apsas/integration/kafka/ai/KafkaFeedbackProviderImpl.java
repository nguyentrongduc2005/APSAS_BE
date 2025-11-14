package com.project.apsas.integration.kafka.ai;

import com.project.apsas.dto.event.FeedbackEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaFeedbackProviderImpl implements KafkaFeedbackProvider {

    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void push(String topic, String key, FeedbackEvent event) {
        kafkaTemplate.send(topic, key, event).whenComplete((res, e) -> {
            if (e != null) throw new RuntimeException(e);

            var md = res.getRecordMetadata();
            log.info("✔ Sent {}-{}@{} key={}", md.topic(), md.partition(), md.offset(), key);
        });
    }
}
