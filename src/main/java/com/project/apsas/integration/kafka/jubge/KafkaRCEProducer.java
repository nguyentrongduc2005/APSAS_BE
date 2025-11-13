package com.project.apsas.integration.kafka.jubge;

import com.project.apsas.dto.event.SubmitCodeEvent;

public interface KafkaRCEProducer {
    void push(String topic, String key, SubmitCodeEvent event);
}
