package com.project.apsas.integration.kafka.ai;


import com.project.apsas.dto.event.FeedbackEvent;

public interface KafkaFeedbackProvider {
    public void push(String topic, String key, FeedbackEvent event);
}
