package com.project.apsas.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.apsas.dto.request.NotificationRequest;
import com.project.apsas.dto.response.NotificationResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.UnreadCountResponse;
import com.project.apsas.entity.Enrollment;
import com.project.apsas.entity.Notification;
import com.project.apsas.enums.EnrollmentRole;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.EnrollmentRepository;
import com.project.apsas.repository.NotificationRepository;
import com.project.apsas.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ObjectMapper objectMapper;

    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(Long userId, int page, int limit, Boolean isRead) {
        // Validate page and limit
        int pg = Math.max(page, 1);
        int lm = Math.min(Math.max(limit, 1), 100);
        
        Pageable pageable = PageRequest.of(pg - 1, lm);
        
        Page<Notification> notificationPage;
        if (isRead != null) {
            notificationPage = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        List<NotificationResponse> notifications = notificationPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<NotificationResponse>builder()
                .data(notifications)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(notificationPage.getTotalElements())
                        .totalPages(notificationPage.getTotalPages())
                        .hasNext(notificationPage.hasNext())
                        .hasPrev(notificationPage.hasPrevious())
                        .build())
                .build();
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        Long count = notificationRepository.countUnreadByUserId(userId);
        return UnreadCountResponse.builder()
                .unreadCount(count != null ? count : 0L)
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        
        if (!notification.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
                userId, false, Pageable.unpaged()).getContent();
        
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        
        if (!notification.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void createAssignmentNotification(Long courseId, String assignmentTitle, String message) {
        // Get all students enrolled in the course
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndRole(courseId, EnrollmentRole.STUDENT);
        List<Long> studentIds = enrollments.stream()
                .map(Enrollment::getUserId)
                .collect(Collectors.toList());
        
        // Create payload
        Map<String, String> payload = new HashMap<>();
        payload.put("courseId", courseId.toString());
        payload.put("assignmentTitle", assignmentTitle);
        payload.put("message", message);
        
        String payloadJson = message;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Error creating notification payload", e);
        }
        
        final String finalPayloadJson = payloadJson;
        
        // Create notifications for all students
        List<Notification> notifications = studentIds.stream()
                .map(studentId -> Notification.builder()
                        .userId(studentId)
                        .type("ASSIGNMENT")
                        .payload(finalPayloadJson)
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());
        
        notificationRepository.saveAll(notifications);
        log.info("Created {} assignment notifications for course {}", notifications.size(), courseId);
    }

    @Override
    @Transactional
    public void createHelpRequestNotification(Long teacherId, Long studentId, String studentName, String helpRequestTitle) {
        // Create payload
        Map<String, String> payload = new HashMap<>();
        payload.put("studentId", studentId.toString());
        payload.put("studentName", studentName);
        payload.put("helpRequestTitle", helpRequestTitle);
        payload.put("message", studentName + " đã yêu cầu trợ giúp: " + helpRequestTitle);
        
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Error creating notification payload", e);
            payloadJson = studentName + " đã yêu cầu trợ giúp: " + helpRequestTitle;
        }
        
        // Create notification for teacher
        Notification notification = Notification.builder()
                .userId(teacherId)
                .type("HELP_REQUEST")
                .payload(payloadJson)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
        log.info("Created help request notification for teacher {}", teacherId);
    }

    @Override
    @Transactional
    public void createSubmissionNotification(Long courseId, Long assignmentId, Long studentId, String studentName, String assignmentTitle) {
        // Get course to find teacher
        List<Enrollment> teacherEnrollments = enrollmentRepository.findByCourseIdAndRole(courseId, EnrollmentRole.TEACHER);
        
        if (teacherEnrollments.isEmpty()) {
            log.warn("No teacher found for course {}", courseId);
            return;
        }
        
        // Create payload
        Map<String, String> payload = new HashMap<>();
        payload.put("courseId", courseId.toString());
        payload.put("assignmentId", assignmentId.toString());
        payload.put("studentId", studentId.toString());
        payload.put("studentName", studentName);
        payload.put("assignmentTitle", assignmentTitle);
        payload.put("message", studentName + " đã nộp bài tập: " + assignmentTitle);
        
        String payloadJson = studentName + " đã nộp bài tập: " + assignmentTitle;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Error creating notification payload", e);
        }
        
        final String finalPayloadJson = payloadJson;
        
        // Create notifications for all teachers
        List<Notification> notifications = teacherEnrollments.stream()
                .map(enrollment -> Notification.builder()
                        .userId(enrollment.getUserId())
                        .type("SUBMISSION")
                        .payload(finalPayloadJson)
                        .isRead(false)
                        .build())
                .collect(Collectors.toList());
        
        notificationRepository.saveAll(notifications);
        log.info("Created {} submission notifications for assignment {} in course {}", 
                notifications.size(), assignmentId, courseId);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .payload(request.getPayload())
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        // Parse payload JSON để lấy message/content
        String message = null;
        Map<String, Object> payloadMap = null;
        
        if (notification.getPayload() != null && !notification.getPayload().isBlank()) {
            try {
                // Thử parse payload như JSON object
                @SuppressWarnings("unchecked")
                Map<String, Object> parsedMap = objectMapper.readValue(notification.getPayload(), Map.class);
                payloadMap = parsedMap;
                // Lấy message từ payload nếu có
                if (payloadMap != null && payloadMap.containsKey("message")) {
                    message = payloadMap.get("message").toString();
                }
            } catch (Exception e) {
                // Nếu không phải JSON, dùng payload như string message
                message = notification.getPayload();
            }
        }
        
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .payload(notification.getPayload())
                .message(message) // Thêm message đã parse
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
