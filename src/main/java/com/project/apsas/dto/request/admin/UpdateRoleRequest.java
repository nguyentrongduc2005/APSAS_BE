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
    
    @Size(max = 80, message = "Tên vai trò không được vượt quá 80 ký tự")
    String name;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    String description;
    
    Set<Long> permissionIds;
}
