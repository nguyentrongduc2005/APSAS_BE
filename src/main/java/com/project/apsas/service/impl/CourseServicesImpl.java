package com.project.apsas.service.impl;

import com.project.apsas.dto.response.*;
import com.project.apsas.entity.Course;
import com.project.apsas.entity.User;
import com.project.apsas.enums.CourseVisibility;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.CourseAssignmentRepository;
import com.project.apsas.repository.CourseContentRepository;
import com.project.apsas.repository.CourseRepository;
import com.project.apsas.repository.EnrollmentRepository;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.CourseServices;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServicesImpl implements CourseServices {

    private final CourseRepository courseRepository;
    private final CourseContentRepository courseContentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Override
    public Page<PublicCourseItem> getPublicCourses(Pageable pageable, String search) {

        Page<Course> courses;
        if (search != null && !search.trim().isEmpty()) {
            courses = courseRepository.findByNameContainingIgnoreCaseAndVisibility(search, CourseVisibility.PUBLIC, pageable);
        }else {
            courses = courseRepository.findByVisibility(CourseVisibility.PUBLIC ,pageable);
        }

        List<Long> courseIds = courses.getContent().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        // Lấy số lượng học viên cho TẤT CẢ khóa học trong trang hiện tại chỉ bằng MỘT truy vấn (dùng IN)
        List<Object[]> studentCountsList = enrollmentRepository.findStudentCountsByCourseIds(courseIds);
        Map<Long, Long> studentsCountMap = studentCountsList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // Key: courseId (đảm bảo là Long)
                        row -> (Long) row[1]  // Value: count (đảm bảo là Long)
                ));

        // --- Lấy tổng số bài học ---
        List<Object[]> totalLessonsList = courseContentRepository.findTotalLessonsByCourseIds(courseIds);
        Map<Long, Long> totalLessonsCountMap = totalLessonsList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // --- Lấy số bài học Public ---
        List<Object[]> publicLessonsList = courseAssignmentRepository.findAssignmentLessonsByCourseIds(courseIds);
        Map<Long, Long> publicLessonsCountMap = publicLessonsList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        // 3. Ánh xạ Page<Course> sang Page<PublicCourseItem>
        return courses.map(course -> convertToPublicCourseItem(
                course,
                studentsCountMap,
                totalLessonsCountMap,
                publicLessonsCountMap
        ));
    }

    private PublicCourseItem convertToPublicCourseItem(
            Course course,
            Map<Long, Long> studentsCountMap,
            Map<Long, Long> totalLessonsCountMap,
            Map<Long, Long> lessonsCountMap
    ) {
        Long courseId = course.getId();

        // Lấy giá trị từ Map, nếu không có thì mặc định là 0
        Long studentsCount = studentsCountMap.getOrDefault(courseId, 0L);
        Long lessonsCountTotal = totalLessonsCountMap.getOrDefault(courseId, 0L);
        Long lessonsCount = lessonsCountMap.getOrDefault(courseId, 0L);

        return PublicCourseItem.builder()
                .id(courseId)
                .name(course.getName())
                .description(course.getDescription())
                .studentsCount(studentsCount)
                .lessonsCount(lessonsCount)
                .lessonsCountTotal(lessonsCountTotal)
                .build();
    }

//    @Override
//    public CourseItemResponse create(CreateCourseRequest req) {
//        // Validate: mã khóa học không trùng
//        String code = req.getCode() == null ? "" : req.getCode().trim();
//        if (code.isEmpty()) {
//            throw new AppException(ErrorCode.BAD_REQUEST); // hoặc dùng ErrorCode phù hợp hệ thống bạn
//        }
//        if (courseRepository.existsByCode(code)) {
//            throw new AppException(ErrorCode.DUPLICATE);   // đã tồn tại mã
//        }
//
//        // Map Request -> Entity (KHÔNG set description vì entity không có field này)
//        Course entity = new Course();
//        entity.setCode(code);
//        entity.setName(req.getName() == null ? "" : req.getName().trim());
//        if (entity.getName().isEmpty()) {
//            throw new AppException(ErrorCode.BAD_REQUEST);
//        }
//        entity.setVisibility(req.getVisibility());
//
//        // Nếu Course của bạn có @CreationTimestamp thì có thể bỏ dòng này
//        try {
//            entity.setCreatedAt(LocalDateTime.now());
//        } catch (Exception ignored) {
//            // nếu entity không có setter createdAt thì bỏ qua
//        }
//
//        Course saved = courseRepository.save(entity);
//
//        // Map Entity -> Response (KHÔNG set description vì DTO của bạn không có)
//        return CourseItemResponse.builder()
//                .id(saved.getId())
//                .code(saved.getCode())
//                .name(saved.getName())
//                .visibility(saved.getVisibility() == null ? null : saved.getVisibility().name())
//                .createdAt(saved.getCreatedAt())
//                .build();
//    }


    @Override
    public CourseRegisResponse getCourseRegistrationDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // 2. Chuẩn bị List ID cho truy vấn BATCH (Chỉ cần 1 phần tử)
        List<Long> singleCourseIdList = List.of(courseId);

        // 3. Thực hiện BATCH COUNTING - Lấy các List<Object[]>

        // Học viên
        List<Object[]> studentCountsList = enrollmentRepository.findStudentCountsByCourseIds(singleCourseIdList);

        // Tổng số nội dung (Bài học)
        List<Object[]> totalLessonsList = courseContentRepository.findTotalLessonsByCourseIds(singleCourseIdList);

        // Tổng số Assignment
        List<Object[]> assignmentCountsList = courseAssignmentRepository.findAssignmentLessonsByCourseIds(singleCourseIdList);

        // --- SỬA LỖI LỚN: CHUYỂN ĐỔI LIST SANG MAP TRƯỚC KHI SỬ DỤNG ---

        // Sẽ chỉ có tối đa 1 phần tử trong List, nhưng vẫn dùng Stream để đảm bảo an toàn kiểu dữ liệu (Long)
        Map<Long, Long> studentsCountMap = listToObjectMap(studentCountsList);
        Map<Long, Long> totalLessonsCountMap = listToObjectMap(totalLessonsList);
        Map<Long, Long> assignmentCountsMap = listToObjectMap(assignmentCountsList);

        // 4. Lấy giá trị chính xác từ Map (đã được đảm bảo là Long -> Long)
        Long totalStudents = studentsCountMap.getOrDefault(courseId, 0L);
        Long lessonsCountTotal = totalLessonsCountMap.getOrDefault(courseId, 0L);
        Long assignmentsCount = assignmentCountsMap.getOrDefault(courseId, 0L);

        // 5. Ánh xạ và trả về DTO
        return buildCourseRegisResponse(
                course,
                totalStudents,
                lessonsCountTotal,
                assignmentsCount
        );
    }
    private Map<Long, Long> listToObjectMap(List<Object[]> list) {
        if (list == null || list.isEmpty()) {
            return Map.of();
        }
        return list.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // Khóa
                        row -> (Long) row[1]  // Giá trị
                ));
    }
    private CourseRegisResponse buildCourseRegisResponse(
            Course course,
            Long totalStudents,
            Long lessonsCountTotal, // Total lessons (total content)
            Long assignmentsCount
    ) {
        // 1. Lấy Entity người tạo
        User creator = course.getCreator(); // Giả định quan hệ @ManyToOne hoạt động

        // 2. TÍNH TOÁN các trường cần thiết cho Giảng viên (InstructorInfo)
        Long creatorId = creator.getId();

        // Giả định: Bạn đã viết các phương thức Repository/Service để lấy các giá trị này
        // CẦN THIẾT: Giả định các hàm này tồn tại hoặc bạn phải tự tính toán:
        Long coursesCountByCreator = courseRepository.countCoursesByCreatorId(creatorId);
        Long totalStudentViews = enrollmentRepository.countTotalStudentsByCreatorId(creatorId);

        // 3. Ánh xạ Instructor Info (Đã truyền giá trị vào)
        CourseRegisResponse.InstructorInfo instructorInfo = CourseRegisResponse.InstructorInfo.builder()
                .id(creatorId)
                .name(creator.getName())
                .email(creator.getEmail())
                .coursesCount(coursesCountByCreator) // <--- TRUYỀN GIÁ TRỊ TÍNH TOÁN
                .studentViews(totalStudentViews)     // <--- TRUYỀN GIÁ TRỊ TÍNH TOÁN
                .build();


        return CourseRegisResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .totalStudents(totalStudents)
                .lessonsCount(lessonsCountTotal)
                .totalAssignments(assignmentsCount)
                .instructor(instructorInfo)
                .build();
    }
    @Override
    public Page<CourseItemTeacherResponse> getMyCoursesTeacher(Pageable pageable, String search) {
        long userId = Long.parseLong(authService.currentId());
        User Student = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.NOT_FOUND));

        Page<Course> courses;
        if (search != null && !search.trim().isEmpty()) {
            courses = courseRepository.findByNameContainingIgnoreCaseAndEnrollmentsContains(search, Student, pageable);
        }else {
            courses = courseRepository.findByEnrollmentsContains(Student, pageable);
        }

        List<Long> courseIds = courses.getContent().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        // Lấy số lượng học viên cho TẤT CẢ khóa học trong trang hiện tại chỉ bằng MỘT truy vấn (dùng IN)
        List<Object[]> studentCountsList = enrollmentRepository.findStudentCountsByCourseIds(courseIds);
        Map<Long, Long> studentsCountMap = studentCountsList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // Key: courseId (đảm bảo là Long)
                        row -> (Long) row[1]// Value: count (đảm bảo là Long)
                ));

        // --- Lấy tổng số bài học ---
        List<Object[]> totalLessonsList = courseContentRepository.findTotalLessonsByCourseIds(courseIds);
        Map<Long, Long> totalLessonsCountMap = totalLessonsList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        // 3. Ánh xạ Page<Course> sang Page<PublicCourseItem>
        return courses.map(course -> mapToCourseItemResponse(
                course,
                studentsCountMap,
                totalLessonsCountMap
        ));
    }

    @Override
    public Page<CourseItemStudentResponse> getMyCoursesStudent(Pageable pageable, String search) {


        return null;
    }

    private CourseItemTeacherResponse mapToCourseItemResponse(
            Course course,
            Map<Long, Long> studentsCountMap,
            Map<Long, Long> totalLessonsCountMap
    ) {
        Long courseId = course.getId();

        // Lấy giá trị từ Map, nếu không có thì mặc định là 0
        Long studentsCount = studentsCountMap.getOrDefault(courseId, 0L);
        Long lessonsCountTotal = totalLessonsCountMap.getOrDefault(courseId, 0L);

        return CourseItemTeacherResponse.builder()
                .id(courseId)
                .name(course.getName())
                .type(course.getType())
                .limit(course.getLimit())
                .avatarUrl(course.getAvatarUrl())
                .currentMember(studentsCount)
                .totalLession(lessonsCountTotal)
                .visibility(course.getVisibility().name())
                .build();
    }
}
