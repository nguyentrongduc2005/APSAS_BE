package com.project.apsas.service.impl;

import com.project.apsas.dto.response.HelpRequestResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.entity.Course;
import com.project.apsas.entity.HelpRequest;
import com.project.apsas.entity.User;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.CourseRepository;
import com.project.apsas.repository.EnrollmentRepository;
import com.project.apsas.repository.HelpRequestsRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.HelpRequestService;
import com.project.apsas.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class HelpRequestServiceImpl implements HelpRequestService {
    HelpRequestsRepository helpRequestsRepository;
    NotificationService notificationService;
    AuthService authService;
    UserRepository userRepository;
    CourseRepository courseRepository;
    EnrollmentRepository enrollmentRepository;

    @Override
    public PagedResponse<HelpRequestResponse> getHelpRequestsByCourse(Long courseId, int page, int limit) {
        // Validate page: minimum 1
        int pg = Math.max(page, 1);
        // Validate limit: minimum 1, maximum 100 items per page (optimized for server performance)
        int lm = Math.min(Math.max(limit, 1), 100);

        Pageable pageable = PageRequest.of(pg - 1, lm);
        Page<HelpRequest> results = helpRequestsRepository.findByCourseId(courseId, pageable);

        List<HelpRequestResponse> data = results.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        int totalPages = results.getTotalPages();
        boolean hasNext = results.hasNext();
        boolean hasPrev = results.hasPrevious();

        return PagedResponse.<HelpRequestResponse>builder()
                .data(data)
                .pagination(PagedResponse.Pagination.builder()
                        .page(pg)
                        .limit(lm)
                        .totalItems(results.getTotalElements())
                        .totalPages(totalPages)
                        .hasNext(hasNext)
                        .hasPrev(hasPrev)
                        .build())
                .build();
    }

    @Override
    public HelpRequestResponse createHelpRequest(Long courseId, String title, String body) {
        Long userId = Long.parseLong(authService.currentId());
        
        // Verify user is enrolled in the course
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (!isEnrolled) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        
        // Get user and course info
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        
        // Create help request
        HelpRequest helpRequest = HelpRequest.builder()
                .userId(userId)
                .courseId(courseId)
                .title(title)
                .body(body)
                .build();
        
        helpRequest = helpRequestsRepository.save(helpRequest);
        log.info("Created help request {} for student {} in course {}", helpRequest.getId(), userId, courseId);
        
        // Notify teacher (course creator)
        Long teacherId = course.getCreator().getId();
        try {
            notificationService.createHelpRequestNotification(
                    teacherId, 
                    userId, 
                    student.getName(), 
                    title
            );
            log.info("Created help request notification for teacher {}", teacherId);
        } catch (Exception e) {
            log.error("Failed to create help request notification", e);
        }
        
        return mapToResponse(helpRequest);
    }

    private HelpRequestResponse mapToResponse(HelpRequest hr) {
        return HelpRequestResponse.builder()
                .id(hr.getId())
                .courseId(hr.getCourseId())
                .title(hr.getTitle())
                .body(hr.getBody())
                .createdAt(hr.getCreatedAt())
                .studentId(hr.getUserId())
                .studentName(hr.getUser() != null ? hr.getUser().getName() : null)
                .studentEmail(hr.getUser() != null ? hr.getUser().getEmail() : null)
                .build();
    }
}
