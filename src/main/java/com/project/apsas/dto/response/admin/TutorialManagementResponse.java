package com.project.apsas.dto.response.admin;

import com.project.apsas.enums.TutorialStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorialManagementResponse {
    Long id;
    String title;
    String summary;
    TutorialStatus status;
    Long createdBy;
    String createdByUsername;
    String createdByEmail;
    LocalDateTime createdAt;
    Integer totalContents;
    Integer totalAssignments;
}
