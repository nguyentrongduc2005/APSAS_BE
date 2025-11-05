package com.project.apsas.service;

import java.util.Properties;

public interface MailService {
    public String sendTransactionalEmail(
            String toEmail, String name, Properties templateParams );
}
