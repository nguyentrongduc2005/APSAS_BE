package com.project.apsas.dto.request.admin;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRoleRequest {
    
    @Size(max = 80, message = "ROLE_NAME_TOO_LONG")
    String name;
    
    @Size(max = 500, message = "ROLE_DESCRIPTION_TOO_LONG")
    String description;
    
    Set<String> permissionNames;
}
