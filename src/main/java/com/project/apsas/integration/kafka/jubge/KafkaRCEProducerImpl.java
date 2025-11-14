package com.project.apsas.integration.kafka.jubge;

import com.project.apsas.dto.event.SubmitCodeEvent;
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
public class KafkaRCEProducerImpl implements KafkaRCEProducer {

    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void push(String topic, String key, SubmitCodeEvent event) {
        kafkaTemplate.send(topic, key, event).whenComplete((res, e) -> {
            if (e != null) throw new RuntimeException(e);
            System.out.println(event.getCode());
            var md = res.getRecordMetadata();
            log.info("✅ Sent {}-{}@{} key={}", md.topic(), md.partition(), md.offset(), key);
        });
    }
}
