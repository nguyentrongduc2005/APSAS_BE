package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.admin.CreateUserRequest;
import com.project.apsas.dto.request.admin.UpdateUserRoleRequest;
import com.project.apsas.dto.request.admin.UpdateUserStatusRequest;
import com.project.apsas.dto.response.admin.UserManagementResponse;
import com.project.apsas.dto.response.admin.UserStatisticsResponse;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserManagementController {

    UserManagementService userManagementService;

    /**
     * Lấy danh sách người dùng với phân trang và filter
     * GET /api/admin/users?page=0&size=10&sort=createdAt,desc&search=nguyen&status=ACTIVE&role=STUDENT
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public ApiResponse<Page<UserManagementResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String role
    ) {
        // Fix: Handle sort array safely
        Sort sortObj;
        if (sort != null && sort.length >= 2) {
            sortObj = Sort.by(
                    sort[1].equalsIgnoreCase("asc") ? Sort.Order.asc(sort[0]) : Sort.Order.desc(sort[0])
            );
        } else if (sort != null && sort.length == 1) {
            sortObj = Sort.by(Sort.Order.desc(sort[0]));
        } else {
            sortObj = Sort.by(Sort.Order.desc("createdAt"));
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<UserManagementResponse> users = userManagementService.getAllUsers(
                pageable, search, status, role
        );

        return ApiResponse.<Page<UserManagementResponse>>builder()
                .code("ok")
                .message("Lấy danh sách người dùng thành công")
                .data(users)
                .build();
    }

    /**
     * Lấy thông tin chi tiết một người dùng
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public ApiResponse<UserManagementResponse> getUserById(@PathVariable Long userId) {
        UserManagementResponse user = userManagementService.getUserById(userId);
        
        return ApiResponse.<UserManagementResponse>builder()
                .code("ok")
                .message("Lấy thông tin người dùng thành công")
                .data(user)
                .build();
    }

    /**
     * Tạo người dùng mới (bởi admin)
     * POST /api/admin/users
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USERS')")
    public ApiResponse<UserManagementResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserManagementResponse user = userManagementService.createUser(request);
        
        return ApiResponse.<UserManagementResponse>builder()
                .code("ok")
                .message("Tạo người dùng thành công")
                .data(user)
                .build();
    }

    /**
     * Cập nhật trạng thái người dùng
     * PUT /api/admin/users/{userId}/status
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('UPDATE_USERS')")
    public ApiResponse<UserManagementResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserManagementResponse user = userManagementService.updateUserStatus(userId, request);
        
        return ApiResponse.<UserManagementResponse>builder()
                .code("ok")
                .message("Cập nhật trạng thái người dùng thành công")
                .data(user)
                .build();
    }

    /**
     * Cập nhật vai trò của người dùng
     * PUT /api/admin/users/{userId}/roles
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('UPDATE_USERS')")
    public ApiResponse<UserManagementResponse> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UserManagementResponse user = userManagementService.updateUserRoles(userId, request);
        
        return ApiResponse.<UserManagementResponse>builder()
                .code("ok")
                .message("Cập nhật roles người dùng thành công")
                .data(user)
                .build();
    }

    /**
     * Xóa người dùng
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('DELETE_USERS')")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        
        return ApiResponse.<Void>builder()
                .code("ok")
                .message("Xóa người dùng thành công")
                .build();
    }

    /**
     * Lấy thống kê người dùng
     * GET /api/admin/users/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public ApiResponse<UserStatisticsResponse> getUserStatistics() {
        UserStatisticsResponse statistics = userManagementService.getUserStatistics();
        
        return ApiResponse.<UserStatisticsResponse>builder()
                .code("ok")
                .message("Lấy thống kê người dùng thành công")
                .data(statistics)
                .build();
    }
}
