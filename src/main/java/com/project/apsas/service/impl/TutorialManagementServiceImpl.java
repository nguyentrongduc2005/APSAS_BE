package com.project.apsas.service.impl;

import com.project.apsas.dto.request.admin.ReviewTutorialRequest;
import com.project.apsas.dto.response.admin.TutorialManagementResponse;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.entity.User;
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

import jakarta.persistence.criteria.Join;
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
    public TutorialManagementResponse reviewTutorial(Long tutorialId, ReviewTutorialRequest request) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // Validate status - chỉ cho phép PUBLISHED hoặc REJECTED
        if (request.getStatus() != TutorialStatus.PUBLISHED && 
            request.getStatus() != TutorialStatus.REJECTED) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Chỉ review tutorial đang PENDING
        if (tutorial.getStatus() != TutorialStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Update tutorial status
        tutorial.setStatus(request.getStatus());

        // Update tất cả content status theo tutorial
        ContentStatus contentStatus = request.getStatus() == TutorialStatus.PUBLISHED 
                ? ContentStatus.PUBLISHED 
                : ContentStatus.REJECTED;
        
        tutorial.getContents().forEach(content -> {
            content.setStatus(contentStatus);
        });

        Tutorial updatedTutorial = tutorialRepository.save(tutorial);
        log.info("Admin reviewed tutorial {} with status: {}", tutorialId, request.getStatus());

        return mapToTutorialManagementResponse(updatedTutorial);
    }

    @Override
    @Transactional
    public Boolean deleteTutorial(Long tutorialId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // Chỉ xóa tutorial DRAFT hoặc REJECTED
        if (tutorial.getStatus() != TutorialStatus.DRAFT && 
            tutorial.getStatus() != TutorialStatus.REJECTED) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        tutorialRepository.delete(tutorial);
        log.info("Admin deleted tutorial {}", tutorialId);
        return true;
    }

    private TutorialManagementResponse mapToTutorialManagementResponse(Tutorial tutorial) {
        return TutorialManagementResponse.builder()
                .id(tutorial.getId())
                .title(tutorial.getTitle())
                .summary(tutorial.getSummary())
                .status(tutorial.getStatus())
                .createdBy(tutorial.getCreatedBy())
                .createdByUsername(null) // Tutorial không có relationship với User
                .createdByEmail(null)
                .createdAt(tutorial.getCreatedAt())
                .totalContents(tutorial.getContents() != null ? tutorial.getContents().size() : 0)
                .totalAssignments(tutorial.getAssignments() != null ? tutorial.getAssignments().size() : 0)
                .build();
    }
}
