package com.project.apsas.service;

import com.project.apsas.dto.request.admin.CreateUserRequest;
import com.project.apsas.dto.request.admin.UpdateUserRoleRequest;
import com.project.apsas.dto.request.admin.UpdateUserStatusRequest;
import com.project.apsas.dto.response.admin.UserManagementResponse;
import com.project.apsas.dto.response.admin.UserStatisticsResponse;
import com.project.apsas.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserManagementService {
    /**
     * Lấy danh sách người dùng với phân trang và filter
     */
    Page<UserManagementResponse> getAllUsers(Pageable pageable, String search, UserStatus status, String role);

    /**
     * Lấy thông tin chi tiết một người dùng
     */
    UserManagementResponse getUserById(Long userId);

    /**
     * Tạo người dùng mới (bởi admin)
     */
    UserManagementResponse createUser(CreateUserRequest request);

    /**
     * Cập nhật trạng thái người dùng (Active, Blocked, Unverified)
     */
    UserManagementResponse updateUserStatus(Long userId, UpdateUserStatusRequest request);

    /**
     * Cập nhật roles của người dùng
     */
    UserManagementResponse updateUserRoles(Long userId, UpdateUserRoleRequest request);

    /**
     * Xóa người dùng
     */
    void deleteUser(Long userId);

    /**
     * Lấy thống kê người dùng
     */
    UserStatisticsResponse getUserStatistics();
}
