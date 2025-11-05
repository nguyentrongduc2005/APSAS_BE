package com.project.apsas.service.impl;

import com.project.apsas.dto.event.SendMailEvent;
import com.project.apsas.integration.brevo.BrevoApiClient;
import com.project.apsas.integration.brevo.dto.SendEmailRequest;
import com.project.apsas.integration.brevo.dto.SendEmailResponse;
import com.project.apsas.service.MailService;
import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailServiceImpl implements MailService {

    BrevoApiClient brevoApiClient;
    @NonFinal
    @Value("${brevo.sender.mail}")
    String senderEmail;
    @NonFinal
    @Value("${brevo.sender.name}")
    String senderName;

    @NonFinal
    @Value("${brevo.template-id}")
    int templateId;

    @Override
    @Async
    public void sendTransactionalEmail(String toEmail, String name, Properties templateParams) {
        SendEmailRequest.Sender sender = SendEmailRequest.Sender.builder()
                .email(senderEmail)
                .name(senderName)
                .build();

        SendEmailRequest.Recipient to =  SendEmailRequest.Recipient.builder()
                .email(toEmail)
                .name(name)
                .build();

        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .params(templateParams)
                .templateId(templateId)
                .sender(sender)
                .to(List.of(to))
                .build();
        try {
            SendEmailResponse response = brevoApiClient.sendEmail(sendEmailRequest);
            System.out.println("Gửi email (Feign) thành công! Message ID: " + response.getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("send mail failed");
        }
    }

    @Override
//    @KafkaListener(id = "mail-listener",topics = "topic-mail")
    public void sendMailConsumer(SendMailEvent event) {
//        System.out.println(event.toString());
//        sendTransactionalEmail(
//                event.getToEmail(),
//                event.getName(),
//                event.getParams()
//        );
    }
}
