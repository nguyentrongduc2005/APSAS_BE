package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.request.NotificationRequest;
import com.project.apsas.dto.response.NotificationResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.UnreadCountResponse;
import com.project.apsas.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_NOTIFICATIONS')")
    public ApiResponse<PagedResponse<NotificationResponse>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Boolean isRead
    ) {
        Long userId = Long.parseLong(authentication.getName());
        PagedResponse<NotificationResponse> notifications = notificationService.getMyNotifications(userId, page, limit, isRead);
        
        return ApiResponse.<PagedResponse<NotificationResponse>>builder()
                .code("ok")
                .message("NOTIFICATIONS_RETRIEVED_SUCCESSFULLY")
                .data(notifications)
                .build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('VIEW_NOTIFICATIONS')")
    public ApiResponse<UnreadCountResponse> getUnreadCount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        UnreadCountResponse count = notificationService.getUnreadCount(userId);
        
        return ApiResponse.<UnreadCountResponse>builder()
                .code("ok")
                .message("UNREAD_COUNT_RETRIEVED_SUCCESSFULLY")
                .data(count)
                .build();
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAuthority('VIEW_NOTIFICATIONS')")
    public ApiResponse<String> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        notificationService.markAsRead(id, userId);
        
        return ApiResponse.<String>builder()
                .code("ok")
                .message("NOTIFICATION_MARKED_AS_READ")
                .data("Notification marked as read successfully")
                .build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAuthority('VIEW_NOTIFICATIONS')")
    public ApiResponse<String> markAllAsRead(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        notificationService.markAllAsRead(userId);
        
        return ApiResponse.<String>builder()
                .code("ok")
                .message("ALL_NOTIFICATIONS_MARKED_AS_READ")
                .data("All notifications marked as read successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_NOTIFICATIONS')")
    public ApiResponse<String> deleteNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        notificationService.deleteNotification(id, userId);
        
        return ApiResponse.<String>builder()
                .code("ok")
                .message("NOTIFICATION_DELETED_SUCCESSFULLY")
                .data("Notification deleted successfully")
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_NOTIFICATIONS')")
    public ApiResponse<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request
    ) {
        NotificationResponse notification = notificationService.createNotification(request);
        
        return ApiResponse.<NotificationResponse>builder()
                .code("ok")
                .message("NOTIFICATION_CREATED_SUCCESSFULLY")
                .data(notification)
                .build();
    }
}
