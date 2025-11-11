package com.project.apsas.service.impl;

import com.project.apsas.dto.request.CreateCourseRequest;
import com.project.apsas.dto.response.CourseItemResponse;
import com.project.apsas.dto.response.PagedResponse;
import com.project.apsas.dto.response.PublicCourseItem;
import com.project.apsas.entity.Course;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.CourseRepository;
import com.project.apsas.service.CourseServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseServicesImpl implements CourseServices {

    private final CourseRepository courseRepository;

    @Override
    public PagedResponse<PublicCourseItem> getPublicCourses(int page, int limit, String search) {
        return null;
    }

    @Override
    public CourseItemResponse create(CreateCourseRequest req) {
        // Validate: mã khóa học không trùng
        String code = req.getCode() == null ? "" : req.getCode().trim();
        if (code.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST); // hoặc dùng ErrorCode phù hợp hệ thống bạn
        }
        if (courseRepository.existsByCode(code)) {
            throw new AppException(ErrorCode.DUPLICATE);   // đã tồn tại mã
        }

        // Map Request -> Entity (KHÔNG set description vì entity không có field này)
        Course entity = new Course();
        entity.setCode(code);
        entity.setName(req.getName() == null ? "" : req.getName().trim());
        if (entity.getName().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        entity.setVisibility(req.getVisibility());

        // Nếu Course của bạn có @CreationTimestamp thì có thể bỏ dòng này
        try {
            entity.setCreatedAt(LocalDateTime.now());
        } catch (Exception ignored) {
            // nếu entity không có setter createdAt thì bỏ qua
        }

        Course saved = courseRepository.save(entity);

        // Map Entity -> Response (KHÔNG set description vì DTO của bạn không có)
        return CourseItemResponse.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .visibility(saved.getVisibility() == null ? null : saved.getVisibility().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
