package com.project.apsas.service.impl;

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

        // Update tutorial status thành PUBLISHED
        tutorial.setStatus(TutorialStatus.PUBLISHED);

        // Update tất cả content status thành PUBLISHED
        if (tutorial.getContents() != null) {
            tutorial.getContents().forEach(content -> {
                content.setStatus(ContentStatus.PUBLISHED);
            });
        }

        // Update tất cả assignment status thành PUBLISHED (nếu có)
        if (tutorial.getAssignments() != null) {
            tutorial.getAssignments().forEach(assignment -> {
                // Assignment không có status field, có thể thêm logic khác nếu cần
                log.debug("Assignment {} is now available for published tutorial {}", 
                    assignment.getId(), tutorialId);
            });
        }

        Tutorial updatedTutorial = tutorialRepository.save(tutorial);
        log.info("Admin published tutorial {} with {} contents and {} assignments", 
            tutorialId, 
            tutorial.getContents() != null ? tutorial.getContents().size() : 0,
            tutorial.getAssignments() != null ? tutorial.getAssignments().size() : 0);

        return mapToTutorialManagementResponse(updatedTutorial);
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
