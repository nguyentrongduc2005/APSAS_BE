package com.project.apsas.service.impl;

import com.project.apsas.dto.request.admin.CreateRoleRequest;
import com.project.apsas.dto.request.admin.UpdateRoleRequest;
import com.project.apsas.dto.response.admin.PermissionResponse;
import com.project.apsas.dto.response.admin.RoleManagementResponse;
import com.project.apsas.entity.Permission;
import com.project.apsas.entity.Role;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.PermissionRepository;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.service.RoleManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleManagementServiceImpl implements RoleManagementService {
    
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    
    @Override
    public List<RoleManagementResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        
        return roles.stream()
                .map(this::mapToRoleManagementResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoleManagementResponse getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        
        return mapToRoleManagementResponse(role);
    }
    
    @Override
    @Transactional
    public RoleManagementResponse createRole(CreateRoleRequest request) {
        // Kiểm tra tên role đã tồn tại chưa
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        
        // Lấy danh sách permissions
        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            permissions = request.getPermissionIds().stream()
                    .map(id -> permissionRepository.findById(id)
                            .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND)))
                    .collect(Collectors.toSet());
        }
        
        // Tạo role mới
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();
        
        role = roleRepository.save(role);
        
        log.info("Created new role: {}", role.getName());
        return mapToRoleManagementResponse(role);
    }
    
    @Override
    @Transactional
    public RoleManagementResponse updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        
        // Cập nhật tên nếu có
        if (request.getName() != null && !request.getName().equals(role.getName())) {
            // Kiểm tra tên mới có trùng không
            if (roleRepository.findByName(request.getName()).isPresent()) {
                throw new AppException(ErrorCode.ROLE_EXISTED);
            }
            role.setName(request.getName());
        }
        
        // Cập nhật mô tả
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        
        // Cập nhật permissions
        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = request.getPermissionIds().stream()
                    .map(id -> permissionRepository.findById(id)
                            .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        role = roleRepository.save(role);
        
        log.info("Updated role: {}", role.getName());
        return mapToRoleManagementResponse(role);
    }
    
    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        
        // Kiểm tra xem role có user nào đang sử dụng không
        if (!role.getUsers().isEmpty()) {
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }
        
        roleRepository.delete(role);
        log.info("Deleted role: {}", role.getName());
    }
    
    @Override
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        
        return permissions.stream()
                .map(permission -> PermissionResponse.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
    
    private RoleManagementResponse mapToRoleManagementResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                .map(permission -> PermissionResponse.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .build())
                .collect(Collectors.toSet());
        
        return RoleManagementResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .userCount(role.getUsers().size())
                .permissions(permissionResponses)
                .build();
    }
}
