package com.project.apsas.service;

import com.project.apsas.dto.event.SendMailEvent;

import java.util.Properties;

public interface MailService {
    public void sendTransactionalEmail(
            String toEmail, String name, Properties templateParams );
    public void sendMailConsumer(SendMailEvent event);
}
