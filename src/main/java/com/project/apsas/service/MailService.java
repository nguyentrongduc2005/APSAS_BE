package com.project.apsas.service;

import com.project.apsas.dto.event.SendMailEvent;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public interface MailService {
    public void sendTransactionalEmail(
            String toEmail, String name, Properties templateParams );
    public CompletableFuture<Void> sendMailAsync(String toEmail, String name, Properties templateParams );
}
