package com.project.apsas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotificationId implements Serializable {
    @Column(name = "users_id")
    Long userId;
    @Column(name = "notifications_id")
    Long notificationId;
}
