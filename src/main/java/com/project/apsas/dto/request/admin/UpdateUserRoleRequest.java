package com.project.apsas.dto.request.admin;

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
public class UpdateUserRoleRequest {
    @NotEmpty(message = "ROLE_IDS_REQUIRED")
    private Set<Long> roleIds;
}
