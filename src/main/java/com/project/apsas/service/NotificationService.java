package com.project.apsas.service;

import com.project.apsas.dto.request.NotificationRequest;
import com.project.apsas.dto.response.NotificationResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.UnreadCountResponse;

public interface NotificationService {
    
    /**
     * Get paginated notifications for a user
     */
    PagedResponse<NotificationResponse> getMyNotifications(Long userId, int page, int limit, Boolean isRead);
    
    /**
     * Get unread notification count for a user
     */
    UnreadCountResponse getUnreadCount(Long userId);
    
    /**
     * Mark a notification as read
     */
    void markAsRead(Long notificationId, Long userId);
    
    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(Long userId);
    
    /**
     * Delete a notification
     */
    void deleteNotification(Long notificationId, Long userId);
    
    /**
     * Create a notification for assignment (teacher -> students)
     */
    void createAssignmentNotification(Long courseId, String assignmentTitle, String message);
    
    /**
     * Create a notification for help request (student -> teacher)
     */
    void createHelpRequestNotification(Long teacherId, Long studentId, String studentName, String helpRequestTitle);
    
    /**
     * Create a notification for submission (student -> teacher)
     */
    void createSubmissionNotification(Long courseId, Long assignmentId, Long studentId, String studentName, String assignmentTitle);
    
    /**
     * Generic method to create notification
     */
    NotificationResponse createNotification(NotificationRequest request);
}
