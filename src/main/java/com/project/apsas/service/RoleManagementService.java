package com.project.apsas.service;

import com.project.apsas.dto.request.admin.CreateRoleRequest;
import com.project.apsas.dto.request.admin.UpdateRoleRequest;
import com.project.apsas.dto.response.admin.PermissionResponse;
import com.project.apsas.dto.response.admin.RoleManagementResponse;

import java.util.List;

public interface RoleManagementService {
    
    /**
     * Lấy danh sách tất cả vai trò
     */
    List<RoleManagementResponse> getAllRoles();
    
    /**
     * Lấy thông tin chi tiết một vai trò
     */
    RoleManagementResponse getRoleById(Long roleId);
    
    /**
     * Tạo vai trò mới
     */
    RoleManagementResponse createRole(CreateRoleRequest request);
    
    /**
     * Cập nhật vai trò
     */
    RoleManagementResponse updateRole(Long roleId, UpdateRoleRequest request);
    
    /**
     * Xóa vai trò
     */
    void deleteRole(Long roleId);
    
    /**
     * Lấy danh sách tất cả permissions
     */
    List<PermissionResponse> getAllPermissions();
}
