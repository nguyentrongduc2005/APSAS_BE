package com.project.apsas.integration.kafka.mail;

import com.project.apsas.dto.event.SendMailEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KafkaMailProducerImpl implements KafkaMailProducer {
    KafkaTemplate<String, Object> kafkaTemplate;
    @Override
    public void push(String topic, String key, SendMailEvent event) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((res, e) -> {
                    if (e != null) throw new RuntimeException(e);

                    var md = res.getRecordMetadata();
                    log.info("✅ Sent {}-{}@{} key={}", md.topic(), md.partition(), md.offset(), key);
                });
    }
}
