package com.project.apsas.dto.response.admin;

import com.project.apsas.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private Long id;
    private String name;
    private String email;
    private UserStatus status;
    private LocalDateTime createdAt;
    private Set<RoleInfo> roles;
    private ProfileInfo profile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInfo {
        private String avatarUrl;
        private String bio;
        private String phone;
    }
}
