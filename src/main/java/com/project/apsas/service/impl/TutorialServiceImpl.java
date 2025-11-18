package com.project.apsas.service.impl;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.request.tutorial.UpdateTutorialRequest;
import com.project.apsas.dto.response.assignment.TutorialAssignmentItemDto;
import com.project.apsas.dto.response.content.TutorialContentItemDto;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.dto.response.tutorial.DetailTutorialResponse;
import com.project.apsas.dto.response.tutorial.TutorialItemDto;
import com.project.apsas.entity.Assignment;
import com.project.apsas.entity.Content;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.entity.User;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.AssignmentRepository;
import com.project.apsas.repository.ContentRepository;
import com.project.apsas.repository.TutorialRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.TutorialService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TutorialServiceImpl implements TutorialService {
    AuthService authService;
    TutorialRepository tutorialRepository;
    UserRepository userRepository;
    AssignmentRepository assignmentRepository;
    ContentRepository contentRepository;

    @Override
    public CreateTutorialResponse createTutorial(CreateTutorialRequest request) {

        Long userId = Long.parseLong(authService.currentId());
        User user = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.NOT_FOUND)
        );

        Tutorial tutorial = Tutorial.builder()
                .createdBy(userId)
                .title(request.getTitle())
                .status(TutorialStatus.DRAFT)
                .summary(request.getSummary())
                .build();

        Tutorial result = tutorialRepository.save(tutorial);

        return CreateTutorialResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .summary(result.getSummary())
                .createdBy(result.getCreatedBy())
                .status(result.getStatus())
                .build();
    }

    @Override
    public List<CreateTutorialResponse> getMyTutorials() {
        Long currentUserId = Long.parseLong(authService.currentId());

        List<Tutorial> tutorials = tutorialRepository.findByCreatedBy(currentUserId);

        return tutorials.stream()
                .map(t -> CreateTutorialResponse.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .summary(t.getSummary())
                        .createdBy(t.getCreatedBy())
                        .status(t.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
    @Override
    public Boolean updateTutorial(UpdateTutorialRequest request, Long tutorialId) {
        Long userId =  Long.parseLong(authService.currentId());

        Tutorial tutorial = tutorialRepository.findById(tutorialId).orElseThrow(() ->
                new AppException(ErrorCode.NOT_FOUND));
        if (!tutorial.getCreatedBy().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);

        if (request.getTitle() != null) {
            tutorial.setTitle(request.getTitle());
        }
        if (request.getSummary() != null) {
            tutorial.setSummary(request.getSummary());
        }

        Tutorial result = tutorialRepository.save(tutorial);

        return result != null;
    }


    @Override
    public DetailTutorialResponse getTutorialDetail(Long tutorialId) {
        // 1. Tìm Tutorial chính
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // 2. Lấy danh sách DTOs đã được xử lý bởi JPQL
        List<TutorialContentItemDto> contents = contentRepository.findContentDTOsByTutorialId(tutorialId);
        List<TutorialAssignmentItemDto> assignments = assignmentRepository.findAssignmentDTOsByTutorialId(tutorialId);

        // 3. Trộn 2 danh sách lại
        List<TutorialItemDto> combinedItems = new ArrayList<>();
        combinedItems.addAll(contents);
        combinedItems.addAll(assignments);

        // 4. Sắp xếp (Logic này vẫn phải nằm ở Java)
        combinedItems.sort(
                Comparator.comparing(
                                TutorialItemDto::getOrderNo,
                                Comparator.nullsLast(Integer::compareTo) // Đẩy item không có orderNo xuống cuối
                        )
                        .thenComparing(
                                TutorialItemDto::getItemType, // Ưu tiên "CONTENT"
                                Comparator.reverseOrder()
                        )
        );

        // 5. Xây dựng và trả về
        return DetailTutorialResponse.builder()
                .id(tutorial.getId())
                .title(tutorial.getTitle())
                .summary(tutorial.getSummary())
                .items(combinedItems)
                .build();
    }

    @Override
    public Boolean submitTutorialForReview(Long tutorialId) {
        Long currentUserId = Long.parseLong(authService.currentId());
        
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));
        
        // Chỉ creator mới submit được
        if (!tutorial.getCreatedBy().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Chỉ submit từ DRAFT
        if (tutorial.getStatus() != TutorialStatus.DRAFT) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        
        // Kiểm tra phải có ít nhất 1 content
        if (tutorial.getContents() == null || tutorial.getContents().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        
        // Chuyển tutorial và tất cả content sang PENDING
        tutorial.setStatus(TutorialStatus.PENDING);
        tutorial.getContents().forEach(content -> {
            content.setStatus(ContentStatus.PENDING);
        });
        
        tutorialRepository.save(tutorial);
        log.info("Provider {} submitted tutorial {} for review", currentUserId, tutorialId);
        
        return true;
    }

}
