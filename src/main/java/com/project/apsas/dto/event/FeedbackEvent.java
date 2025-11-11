package com.project.apsas.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackEvent {
    Long submissionId;
    String code;
    String statement_md;
    String language;
}
