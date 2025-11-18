package com.project.apsas.dto.request.admin;

import com.project.apsas.enums.TutorialStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewTutorialRequest {
    TutorialStatus status; // PUBLISHED or REJECTED
    String reviewNote; // Optional: ghi chú của admin
}
