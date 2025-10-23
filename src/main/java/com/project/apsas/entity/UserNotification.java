package com.project.apsas.entity;

import com.project.apsas.entity.UserNotificationId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserNotificationId.class)
@Table(name = "users_notifications")
public class UserNotification {

    @Id
    @Column(name = "users_id")
    private Long userId;

    @Id
    @Column(name = "notifications_id")
    private Long notificationId;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notifications_id", insertable = false, updatable = false)
    private notifications notification;
}
