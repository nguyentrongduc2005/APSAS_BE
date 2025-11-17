package com.project.apsas.dto.request.admin;

import com.project.apsas.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "USER_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    private String password;

    @NotEmpty(message = "ROLE_IDS_REQUIRED")
    private Set<Long> roleIds;

    @lombok.Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}
