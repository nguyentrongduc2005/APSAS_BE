package com.project.apsas.integration.kafka.mail;

import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.service.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaMailConsumer {

    MailService mailService;
    KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "${message-queue.topic.mail.name}",  groupId = "group_mail")
    public void consume(ConsumerRecord<String, SendMailEvent> rec, Acknowledgment ack){
        ack.acknowledge();
        SendMailEvent event = rec.value();
        mailService.sendMailAsync(event.getToEmail(), event.getName(), event.getParams())
                .exceptionally(throwable -> {
                    sendToDLT(rec, throwable); return null;
                });

    }

    private void sendToDLT(ConsumerRecord<String, SendMailEvent> rec, Throwable ex)
    {
        String dlt = rec.topic() + ".DLT";
        ProducerRecord<String, Object> pr = new ProducerRecord<>(dlt, rec.key(), rec.value());
        pr.headers().add(new RecordHeader("x-ex", ex.toString().getBytes(StandardCharsets.UTF_8)));
        pr.headers().add(new RecordHeader("x-orig-topic", rec.topic().getBytes(StandardCharsets.UTF_8)));
        pr.headers().add(new RecordHeader("x-orig-partition", String.valueOf(rec.partition()).getBytes(StandardCharsets.UTF_8)));
        pr.headers().add(new RecordHeader("x-orig-offset", String.valueOf(rec.offset()).getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(pr).whenComplete((md, dltEx) -> {
            if (dltEx != null) {
                dltEx.printStackTrace();
                throw  new RuntimeException(dltEx);
            }
        });
    }


}
