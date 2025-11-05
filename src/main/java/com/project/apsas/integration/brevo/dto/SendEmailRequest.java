package com.project.apsas.integration.brevo.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Properties;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendEmailRequest {
    Sender sender;
    List<Recipient> to;
    int templateId;
    Properties params;
    @Data
    @Builder
    public static class Sender {
        String email;
        String name;
    }
    @Data
    @Builder
    public static class Recipient {
        String email;
        String name;
    }
}


