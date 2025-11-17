package com.project.apsas.service.impl;

import com.project.apsas.dto.request.admin.ReviewContentRequest;
import com.project.apsas.dto.response.admin.ContentManagementResponse;
import com.project.apsas.dto.response.admin.ContentStatisticsResponse;
import com.project.apsas.entity.Content;
import com.project.apsas.entity.Media;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.entity.User;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.MediaType;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.ContentRepository;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.ContentManagementService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class ContentManagementServiceImpl implements ContentManagementService {

    ContentRepository contentRepository;
    TutorialRepository tutorialRepository;
    UserRepository userRepository;

    @Override
    public Page<ContentManagementResponse> getAllContents(
            Pageable pageable, 
            String search, 
            ContentStatus status, 
            MediaType mediaType
    ) {
        Specification<Content> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by title
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), 
                        searchPattern
                ));
            }

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by media type
            if (mediaType != null) {
                Join<Content, Media> mediaJoin = root.join("mediaList");
                predicates.add(criteriaBuilder.equal(mediaJoin.get("type"), mediaType));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Content> contents = contentRepository.findAll(spec, pageable);
        return contents.map(this::mapToContentManagementResponse);
    }

    @Override
    public ContentManagementResponse getContentById(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_EXISTED));
        return mapToContentManagementResponse(content);
    }

    @Override
    public ContentManagementResponse reviewContent(Long contentId, ReviewContentRequest request) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_EXISTED));

        // Validate status - chỉ cho phép PUBLISHED hoặc REJECTED
        if (request.getStatus() != ContentStatus.PUBLISHED && 
            request.getStatus() != ContentStatus.REJECTED) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // Update content status
        content.setStatus(request.getStatus());
        Content updatedContent = contentRepository.save(content);

        // Nếu PUBLISHED, kiểm tra và cập nhật Tutorial nếu cần
        if (request.getStatus() == ContentStatus.PUBLISHED) {
            Tutorial tutorial = content.getTutorial();
            if (tutorial != null && tutorial.getStatus() != TutorialStatus.PUBLISHED) {
                // Check xem tất cả content của tutorial đã PUBLISHED chưa
                boolean allContentsPublished = tutorial.getContents().stream()
                        .allMatch(c -> c.getStatus() == ContentStatus.PUBLISHED);
                
                if (allContentsPublished) {
                    tutorial.setStatus(TutorialStatus.PUBLISHED);
                    tutorialRepository.save(tutorial);
                    log.info("Tutorial {} auto-published after all contents approved", tutorial.getId());
                }
            }
        }

        log.info("Admin reviewed content {} with status: {}", contentId, request.getStatus());
        return mapToContentManagementResponse(updatedContent);
    }

    @Override
    public void deleteContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTENT_NOT_EXISTED));

        contentRepository.delete(content);
        log.info("Admin deleted content: {}", contentId);
    }

    @Override
    public ContentStatisticsResponse getContentStatistics() {
        long totalContents = contentRepository.count();
        long pendingContents = contentRepository.countByStatus(ContentStatus.PENDING);
        long publishedContents = contentRepository.countByStatus(ContentStatus.PUBLISHED);
        long rejectedContents = contentRepository.countByStatus(ContentStatus.REJECTED);
        long draftContents = contentRepository.countByStatus(ContentStatus.DRAFT);

        return ContentStatisticsResponse.builder()
                .totalContents(totalContents)
                .pendingContents(pendingContents)
                .publishedContents(publishedContents)
                .rejectedContents(rejectedContents)
                .draftContents(draftContents)
                .build();
    }

    private ContentManagementResponse mapToContentManagementResponse(Content content) {
        // Get primary media type (first media or null)
        MediaType primaryMediaType = null;
        if (content.getMediaList() != null && !content.getMediaList().isEmpty()) {
            primaryMediaType = content.getMediaList().stream()
                    .findFirst()
                    .map(Media::getType)
                    .orElse(null);
        }

        // Get author name from tutorial
        String authorName = null;
        if (content.getTutorial() != null && content.getTutorial().getCreatedBy() != null) {
            authorName = userRepository.findById(content.getTutorial().getCreatedBy())
                    .map(User::getName)
                    .orElse("Unknown");
        }

        // Map media list
        List<ContentManagementResponse.MediaInfo> mediaList = null;
        if (content.getMediaList() != null) {
            mediaList = content.getMediaList().stream()
                    .map(media -> ContentManagementResponse.MediaInfo.builder()
                            .id(media.getId())
                            .type(media.getType())
                            .url(media.getUrl())
                            .caption(media.getCaption())
                            .build())
                    .collect(Collectors.toList());
        }

        return ContentManagementResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .primaryMediaType(primaryMediaType)
                .authorName(authorName)
                .createdAt(content.getCreatedAt())
                .status(content.getStatus())
                .tutorialTitle(content.getTutorial() != null ? content.getTutorial().getTitle() : null)
                .tutorialId(content.getTutorialId())
                .bodyMd(content.getBodyMd())
                .mediaList(mediaList)
                .build();
    }
}
