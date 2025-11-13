package com.project.apsas.configuration;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Inject topic names từ application.yaml
    @Value("${message-queue.topic.mail.name}")
    private String topicMailName;

    @Value("${message-queue.topic.mail.dlt}")
    private String topicMailDltName;

    @Value("${message-queue.topic.mail.drop}")
    private String topicMailDropName;

    @Value("${message-queue.topic.feedback.name}")
    private String topicFeedbackName;

    @Value("${app.kafka.topic.verify-email:verify-email}")
    private String topicVerifyEmailName;

    /**
     * KafkaAdmin bean để tạo topic tự động khi khởi động
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Topic chính cho gửi email
     * Được consume bởi KafkaMailConsumer
     */
    @Bean
    public NewTopic topicMail() {
        return TopicBuilder.name(topicMailName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Dead Letter Topic cho topic-mail
     * Các message lỗi từ topic-mail sẽ được chuyển vào đây
     * Được consume bởi KafKaMailDltConsumer với retry logic
     */
    @Bean
    public NewTopic topicMailDLT() {
        return TopicBuilder.name(topicMailDltName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Drop Topic cho các message thất bại hoàn toàn
     * Message từ DLT thất bại sau 5 retry sẽ vào đây (final fallback)
     */
    @Bean
    public NewTopic topicMailDLTDrop() {
        return TopicBuilder.name(topicMailDropName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic cho AI feedback/processing
     * Được consume bởi KafkaFeedbackConsumer để xử lý code review
     */
    @Bean
    public NewTopic topicAI() {
        return TopicBuilder.name(topicFeedbackName)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Dead Letter Topic cho topic-ai
     * KafkaFeedbackConsumer dùng @RetryableTopic với dltTopicSuffix = ".DLT"
     * Topic này sẽ nhận message thất bại sau 3 attempts
     */
    @Bean
    public NewTopic topicAIDLT() {
        return TopicBuilder.name(topicFeedbackName + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Topic cho verify email flow
     * (Dự phòng - có thể được sử dụng trong tương lai)
     */
    @Bean
    public NewTopic topicVerifyEmail() {
        return TopicBuilder.name(topicVerifyEmailName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
