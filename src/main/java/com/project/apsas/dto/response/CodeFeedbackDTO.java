package com.project.apsas.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CodeFeedbackDTO {
    String feedback;
    String suggestion;
    String bigOComplexityTime;
    String bigOComplexitySpace;
}
