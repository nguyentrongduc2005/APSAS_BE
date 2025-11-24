package com.project.apsas.service.impl;

import com.project.apsas.dto.request.tutorial.CreateTutorialRequest;
import com.project.apsas.dto.request.tutorial.UpdateTutorialRequest;
import com.project.apsas.dto.response.assignment.TutorialAssignmentItemDto;
import com.project.apsas.dto.response.content.TutorialContentItemDto;
import com.project.apsas.dto.response.tutorial.CreateTutorialResponse;
import com.project.apsas.dto.response.tutorial.DetailTutorialResponse;
import com.project.apsas.dto.response.tutorial.SearchTutorialResponse;
import com.project.apsas.dto.response.tutorial.TutorialItemDto;
import com.project.apsas.entity.Assignment;
import com.project.apsas.entity.Content;
import com.project.apsas.entity.Tutorial;
import com.project.apsas.entity.User;
import com.project.apsas.enums.ContentStatus;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.*;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.TutorialService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
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
    CourseRepository courseRepository;

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
    public List<CreateTutorialResponse> getMyTutorials(String keyword,
                                                       String status,
                                                       Boolean hasAssignment) {
        Long currentUserId = Long.parseLong(authService.currentId());

        // lấy user để có name + avatar
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String avatar;
        if (user.getProfile() != null) {
            avatar = user.getProfile().getAvatarUrl();  // đổi cho đúng tên getter trong entity Profile
        } else {
            avatar = null;
        }

        // lấy tất cả tutorial do user này tạo
        List<Tutorial> tutorials = tutorialRepository.findByCreatedBy(currentUserId);

        // chuẩn bị keyword (có thể null)
        String kw = (keyword == null || keyword.isBlank())
                ? null
                : keyword.trim().toLowerCase();

        // chuẩn bị filter status (có thể null)
        TutorialStatus filterStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                filterStatus = TutorialStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                // nếu FE gửi status linh tinh thì bỏ qua filter status
                filterStatus = null;
            }
        }

        TutorialStatus finalFilterStatus = filterStatus;
        return tutorials.stream()
                // ---- search theo title + summary ----
                .filter(t -> {
                    if (kw == null) return true;
                    String title = t.getTitle() == null ? "" : t.getTitle().toLowerCase();
                    String summary = t.getSummary() == null ? "" : t.getSummary().toLowerCase();
                    return title.contains(kw) || summary.contains(kw);
                })
                // ---- filter status ----
                .filter(t -> finalFilterStatus == null || t.getStatus() == finalFilterStatus)
                // ---- map sang DTO + TODO: count ----
                .map(t -> {
                    Long tutorialId = t.getId();

                    // TODO: thay 3 dòng dưới bằng logic thực sự
                    int lessonCount = 0;
                    int assignmentCount = 0;
                    int courseCount = 0;

                    // Nếu muốn filter theo hasAssignment:
                    if (hasAssignment != null) {
                        boolean has = assignmentCount > 0;
                        if (hasAssignment && !has) return null;
                        if (!hasAssignment && has) return null;
                    }

                    return CreateTutorialResponse.builder()
                            .id(tutorialId)
                            .title(t.getTitle())
                            .summary(t.getSummary())
                            .status(t.getStatus())
                            .lessonCount(lessonCount)
                            .assignmentCount(assignmentCount)
                            .courseCount(courseCount)
                            .creatorName(user.getName())
                            .creatorAvatar(avatar)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SearchTutorialResponse> searchTutorials(String keyword, Pageable pageable) {
        Specification<Tutorial> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy tutorial đã PUBLISHED
            predicates.add(cb.equal(root.get("status"), TutorialStatus.PUBLISHED));

            // Tìm theo keyword: title hoặc summary
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate summaryLike = cb.like(cb.lower(root.get("summary")), likePattern);
                predicates.add(cb.or(titleLike, summaryLike));
            }

            // Để tránh lỗi N+1 khi đếm assignments/contents, có thể fetch join nếu cần thiết
            // Tuy nhiên với Pageable, fetch join collection thường gây warning in memory paging.
            // Ở đây dùng Lazy loading mặc định và transaction để xử lý.
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Query lấy danh sách Tutorials
        Page<Tutorial> pageResult = tutorialRepository.findAll(spec, pageable);

        // 3. TỐI ƯU HIỆU NĂNG: Bulk Fetch thông tin Creator
        // Lấy list user ID từ kết quả page
        Set<Long> userIds = pageResult.getContent().stream()
                .map(Tutorial::getCreatedBy)
                .collect(Collectors.toSet());

        // Query 1 lần để lấy map User (Id -> User)
        Map<Long, User> creatorMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 4. Map Entity sang Response DTO
        return pageResult.map(t -> {
            // Lấy user từ map đã cache
            User creator = creatorMap.get(t.getCreatedBy());

            // Xử lý các trường count (bảo vệ null safe)
            int lessonCount = (t.getContents() != null) ? t.getContents().size() : 0;
            int assignmentCount = (t.getAssignments() != null) ? t.getAssignments().size() : 0;

            // TODO: Logic cho mediaCount và courseCount
            // Nếu Content có trường type là VIDEO/IMAGE, bạn có thể filter ở đây
            int mediaCount = 0;
            assert t.getContents() != null;
            for(Content content : t.getContents()) {
                if(content.getMediaList() != null) {
                    mediaCount += content.getMediaList().size();
                }
            }
            // Nếu cần đếm Course, gọi repo: courseRepository.countByTutorialId(t.getId())
            // Lưu ý: Gọi repo trong loop map() sẽ không tốt cho hiệu năng nếu list lớn.


            return SearchTutorialResponse.builder()
                    .id(t.getId())
                    .title(t.getTitle())
                    .summary(t.getSummary())
                    .status(t.getStatus())
                    // Mapping các số liệu thống kê
                    .lessonCount(lessonCount)
                    .assignmentCount(assignmentCount)
                    .mediaCount(mediaCount)
                    // Mapping thông tin Creator
                    .creatorName(creator != null ? creator.getName() : "Unknown User") // Giả định User có getFullName
                    .creatorAvatar(creator != null ? creator.getAvatarUrl() : null)           // Giả định User có getAvatar
                    .build();
        });
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
