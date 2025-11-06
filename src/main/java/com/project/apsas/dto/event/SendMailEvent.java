package com.project.apsas.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Properties;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMailEvent {
    String toEmail;
    String name;
    Properties params;
}
