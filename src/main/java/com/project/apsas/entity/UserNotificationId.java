package com.project.apsas.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationId implements Serializable {

    private Long userId;
    private Long notificationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserNotificationId that)) return false;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, notificationId);
    }
}
