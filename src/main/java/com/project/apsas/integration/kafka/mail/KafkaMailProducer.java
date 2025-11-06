package com.project.apsas.integration.kafka.mail;

import com.project.apsas.dto.event.SendMailEvent;

public interface KafkaMailProducer {
    public void push(String topic, String key, SendMailEvent event);
}
