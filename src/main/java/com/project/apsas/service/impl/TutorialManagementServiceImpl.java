package com.project.apsas.service.impl;

import com.project.apsas.dto.request.admin.ReviewTutorialRequest;
import com.project.apsas.dto.response.admin.TutorialManagementResponse;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.service.TutorialManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TutorialManagementServiceImpl implements TutorialManagementService {

    TutorialRepository tutorialRepository;

    @Override
    public Page<TutorialManagementResponse> getAllTutorials(TutorialStatus status, String keyword, Pageable pageable) {
        Specification<Tutorial> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by keyword (title or summary)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), likePattern);
                Predicate summaryMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("summary")), likePattern);
                predicates.add(criteriaBuilder.or(titleMatch, summaryMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Tutorial> tutorialPage = tutorialRepository.findAll(spec, pageable);
        return tutorialPage.map(this::mapToTutorialManagementResponse);
    }

    @Override
    public TutorialManagementResponse getTutorialDetail(Long tutorialId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));
        return mapToTutorialManagementResponse(tutorial);
    }

    @Override
    @Transactional
    public TutorialManagementResponse publishTutorial(Long tutorialId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // Chỉ publish tutorial đang PENDING
        if (tutorial.getStatus() != TutorialStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Force load collections trong transaction để tránh LazyInitializationException
        if (tutorial.getContents() != null) {
            tutorial.getContents().size(); // Force load
        }
        if (tutorial.getAssignments() != null) {
            tutorial.getAssignments().size(); // Force load
        }

        // Update tutorial status thành PUBLISHED
        tutorial.setStatus(TutorialStatus.PUBLISHED);

        // Update tất cả content status thành PUBLISHED
        if (tutorial.getContents() != null && !tutorial.getContents().isEmpty()) {
            tutorial.getContents().forEach(content -> {
                content.setStatus(ContentStatus.PUBLISHED);
            });
        }

        // Update tất cả assignment status thành PUBLISHED (nếu có)
        if (tutorial.getAssignments() != null && !tutorial.getAssignments().isEmpty()) {
            tutorial.getAssignments().forEach(assignment -> {
                // Assignment không có status field, có thể thêm logic khác nếu cần
                log.debug("Assignment {} is now available for published tutorial {}", 
                    assignment.getId(), tutorialId);
            });
        }

        tutorialRepository.save(tutorial);
        log.info("Admin published tutorial {} with {} contents and {} assignments", 
            tutorialId, 
            tutorial.getContents() != null ? tutorial.getContents().size() : 0,
            tutorial.getAssignments() != null ? tutorial.getAssignments().size() : 0);

        // Reload tutorial với collections để tránh LazyInitializationException khi map response
        Tutorial tutorialForResponse = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));
        // Force load collections
        if (tutorialForResponse.getContents() != null) {
            tutorialForResponse.getContents().size();
        }
        if (tutorialForResponse.getAssignments() != null) {
            tutorialForResponse.getAssignments().size();
        }

        return mapToTutorialManagementResponse(tutorialForResponse);
    }

    @Override
    @Transactional
    public TutorialManagementResponse reviewTutorial(Long tutorialId, ReviewTutorialRequest request) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // Chỉ review tutorial đang PENDING
        if (tutorial.getStatus() != TutorialStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể review tutorial đang ở trạng thái PENDING");
        }

        // Validate status phải là PUBLISHED hoặc REJECTED
        if (request.getStatus() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Status không được để trống");
        }
        
        if (request.getStatus() != TutorialStatus.PUBLISHED && request.getStatus() != TutorialStatus.REJECTED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Status phải là PUBLISHED hoặc REJECTED");
        }

        // Force load contents trong transaction để tránh LazyInitializationException
        if (tutorial.getContents() != null) {
            tutorial.getContents().size(); // Force load collection
        }

        // Update tutorial status
        tutorial.setStatus(request.getStatus());

        // Nếu publish thì update content status thành PUBLISHED
        if (request.getStatus() == TutorialStatus.PUBLISHED) {
            if (tutorial.getContents() != null && !tutorial.getContents().isEmpty()) {
                tutorial.getContents().forEach(content -> {
                    content.setStatus(ContentStatus.PUBLISHED);
                });
            }
        } else if (request.getStatus() == TutorialStatus.REJECTED) {
            // Nếu reject thì có thể giữ nguyên content status hoặc set về DRAFT
            // Tùy vào business logic, ở đây mình giữ nguyên
            log.info("Tutorial {} rejected with note: {}", tutorialId, request.getReviewNote());
        }

        tutorialRepository.save(tutorial);
        log.info("Admin reviewed tutorial {} with status {} and note: {}", 
            tutorialId, request.getStatus(), request.getReviewNote());

        // Reload tutorial với collections để tránh LazyInitializationException khi map response
        Tutorial tutorialForResponse = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));
        // Force load collections
        if (tutorialForResponse.getContents() != null) {
            tutorialForResponse.getContents().size();
        }
        if (tutorialForResponse.getAssignments() != null) {
            tutorialForResponse.getAssignments().size();
        }

        return mapToTutorialManagementResponse(tutorialForResponse);
    }

    private TutorialManagementResponse mapToTutorialManagementResponse(Tutorial tutorial) {
        // Đảm bảo collections đã được load trước khi truy cập
        int totalContents = 0;
        int totalAssignments = 0;
        
        try {
            if (tutorial.getContents() != null) {
                totalContents = tutorial.getContents().size();
            }
        } catch (Exception e) {
            log.warn("Error getting contents size for tutorial {}: {}", tutorial.getId(), e.getMessage());
        }
        
        try {
            if (tutorial.getAssignments() != null) {
                totalAssignments = tutorial.getAssignments().size();
            }
        } catch (Exception e) {
            log.warn("Error getting assignments size for tutorial {}: {}", tutorial.getId(), e.getMessage());
        }
        
        return TutorialManagementResponse.builder()
                .id(tutorial.getId())
                .title(tutorial.getTitle())
                .summary(tutorial.getSummary())
                .status(tutorial.getStatus())
                .createdBy(tutorial.getCreatedBy())
                .createdByUsername(null) // Tutorial không có relationship với User
                .createdByEmail(null)
                .createdAt(tutorial.getCreatedAt())
                .totalContents(totalContents)
                .totalAssignments(totalAssignments)
                .build();
    }
}
