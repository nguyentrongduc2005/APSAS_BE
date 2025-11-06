package com.project.apsas.integration.kafka.mail;

import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.service.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class KafKaMailDltConsumer {
    MailService mailService;

    @RetryableTopic(
            attempts = "5",
            dltTopicSuffix = ".DROP",
            autoCreateTopics = "true",
            backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 30000)
    )
    @KafkaListener(topics = "${message-queue.topic.mail.dlt}", groupId = "group_mail_dlt")
    public void consumeDlt(ConsumerRecord<?, ?> rec, Acknowledgment ack) {
        try {
            log.info("▶ processing {}-{}@{} key={} payload={}",
                    rec.topic(), rec.partition(), rec.offset(), rec.key(), rec.value());
            SendMailEvent e =  (SendMailEvent) rec.value();
            mailService.sendTransactionalEmail(e.getToEmail(),e.getName(),e.getParams());
            ack.acknowledge();
            log.info("✔ done {}-{}@{} key={}", rec.topic(), rec.partition(), rec.offset(), rec.key());
        } catch (Exception e) {
            throw e;
        }

    }
}
