package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.admin.CreateRoleRequest;
import com.project.apsas.dto.request.admin.UpdateRoleRequest;
import com.project.apsas.dto.response.admin.PermissionResponse;
import com.project.apsas.dto.response.admin.RoleManagementResponse;
import com.project.apsas.service.RoleManagementService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleManagementController {
    
    RoleManagementService roleManagementService;
    
    /**
     * Lấy danh sách tất cả vai trò
     * GET /api/admin/roles
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public ApiResponse<List<RoleManagementResponse>> getAllRoles() {
        List<RoleManagementResponse> roles = roleManagementService.getAllRoles();
        
        return ApiResponse.<List<RoleManagementResponse>>builder()
                .code("ok")
                .message("Lấy danh sách vai trò thành công")
                .data(roles)
                .build();
    }
    
    /**
     * Lấy thông tin chi tiết một vai trò
     * GET /api/admin/roles/{roleId}
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public ApiResponse<RoleManagementResponse> getRoleById(@PathVariable Long roleId) {
        RoleManagementResponse role = roleManagementService.getRoleById(roleId);
        
        return ApiResponse.<RoleManagementResponse>builder()
                .code("ok")
                .message("Lấy thông tin vai trò thành công")
                .data(role)
                .build();
    }
    
    /**
     * Tạo vai trò mới
     * POST /api/admin/roles
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ROLES')")
    public ApiResponse<RoleManagementResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        RoleManagementResponse role = roleManagementService.createRole(request);
        
        return ApiResponse.<RoleManagementResponse>builder()
                .code("ok")
                .message("Tạo vai trò thành công")
                .data(role)
                .build();
    }
    
    /**
     * Cập nhật vai trò
     * PUT /api/admin/roles/{roleId}
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('UPDATE_ROLES')")
    public ApiResponse<RoleManagementResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleManagementResponse role = roleManagementService.updateRole(roleId, request);
        
        return ApiResponse.<RoleManagementResponse>builder()
                .code("ok")
                .message("Cập nhật vai trò thành công")
                .data(role)
                .build();
    }
    
    /**
     * Xóa vai trò
     * DELETE /api/admin/roles/{roleId}
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('DELETE_ROLES')")
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
        roleManagementService.deleteRole(roleId);
        
        return ApiResponse.<Void>builder()
                .code("ok")
                .message("Xóa vai trò thành công")
                .build();
    }
    
    /**
     * Lấy danh sách tất cả permissions
     * GET /api/admin/permissions
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = roleManagementService.getAllPermissions();
        
        return ApiResponse.<List<PermissionResponse>>builder()
                .code("ok")
                .message("Lấy danh sách quyền thành công")
                .data(permissions)
                .build();
    }
}
