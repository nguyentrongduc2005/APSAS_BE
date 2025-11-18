package com.project.apsas.dto.response.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleManagementResponse {
    Long id;
    String name;
    String description;
    LocalDateTime createdAt;
    Integer userCount;
    Set<PermissionResponse> permissions;
}
