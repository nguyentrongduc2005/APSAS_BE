package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users_notifications")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotification {

    @EmbeddedId
    UserNotificationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "users_id")
    User user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("userId")
//    @JoinColumn(name = "users_id")
//    Notification notification;
    @Column(name = "is_read")
    boolean isRead;

    @Column( updatable = false, insertable = false)
    LocalDateTime readAt;
}
