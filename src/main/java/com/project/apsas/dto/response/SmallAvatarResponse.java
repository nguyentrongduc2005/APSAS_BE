package com.project.apsas.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SmallAvatarResponse {

    Long userId;
    String name;
    String email;
    String avatarUrl;
    String smallAvatarUrl;
    Boolean success;
}
