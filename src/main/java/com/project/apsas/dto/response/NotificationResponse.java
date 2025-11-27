package com.project.apsas.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String type;
    private String payload;
    private String message; // Nội dung thông báo đã được parse từ payload
    private Boolean isRead;
    private LocalDateTime createdAt;
}
