package com.project.apsas.service.impl;

import com.project.apsas.dto.request.CreateCourseFromTutorialRequest;
import com.project.apsas.dto.request.course.JoinCourseRequest;
import com.project.apsas.dto.response.*;
import com.project.apsas.dto.response.course.JoinCourseResponse;
import com.project.apsas.dto.teacher.CreateCourseRequestDTO;
import com.project.apsas.dto.teacher.CreateCourseResponseDTO;
import com.project.apsas.entity.*;
import com.project.apsas.enums.CourseVisibility;
import com.project.apsas.enums.EnrollmentRole;
import com.project.apsas.enums.TutorialStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.*;
import com.project.apsas.service.AuthService;
import com.project.apsas.service.CloudinaryService;
import com.project.apsas.service.CourseServices;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.time.LocalDateTime;
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
    private final AssignmentRepository assignmentRepository;
    private final TutorialRepository tutorialRepository;
    private final ContentRepository contentRepository;
    private final CloudinaryService cloudinaryService;
    private final SubmissionRepository submissionRepository;

    @NonFinal
    @Value("${cloudinary.option.folder-name}")
    String folder;


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
                .url(course.getAvatarUrl())
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
//            throw new AppException(ErrorCode.BAD_REQUEST);

    @Override
    @Transactional
    public CreateCourseResponseDTO createCourse(CreateCourseRequestDTO request) {
        // Get current user
        String currentIdStr = authService.currentId();
        Long creatorId = Long.parseLong(currentIdStr);

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Validate tutorial exists
        Tutorial tutorial = tutorialRepository.findById(request.getTutorialId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));

        // Create course
        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .code(request.getCode())
                .visibility(CourseVisibility.valueOf(request.getVisibility().toUpperCase()))
                .type(request.getType())
                .avatarUrl(request.getAvatarUrl())
                .limit(request.getLimit())
                .creator(creator)
                .build();

        Course savedCourse = courseRepository.save(course);

        // Enroll creator as TEACHER
        Enrollment creatorEnrollment = Enrollment.builder()
                .userId(creatorId)
                .courseId(savedCourse.getId())
                .role(EnrollmentRole.TEACHER)
                .build();
        enrollmentRepository.save(creatorEnrollment);

        // XỬ LÝ CONTENTS (CHỈ ADD NHỮNG CÁI ĐƯỢC CHỌN)
        int contentsAdded = 0;
        if (tutorial.getContents() != null && !tutorial.getContents().isEmpty()
                && request.getSelectedContentIds() != null && !request.getSelectedContentIds().isEmpty()) {

            Set<Long> selectedContentIds = new HashSet<>(request.getSelectedContentIds());

            List<CourseContent> courseContents = tutorial.getContents().stream()
                    .filter(content -> selectedContentIds.contains(content.getId()))
                    .map(content -> CourseContent.builder()
                            .courseId(savedCourse.getId())
                            .contentId(content.getId())
                            .build())
                    .collect(Collectors.toList());

            if (!courseContents.isEmpty()) {
                courseContentRepository.saveAll(courseContents);
                contentsAdded = courseContents.size();
            }
        }

        // XỬ LÝ ASSIGNMENTS (CHỈ ADD NHỮNG CÁI CÓ TRONG SCHEDULE LIST)
        int assignmentsAdded = 0;
        if (tutorial.getAssignments() != null && !tutorial.getAssignments().isEmpty()
                && request.getAssignmentSchedules() != null && !request.getAssignmentSchedules().isEmpty()) {

            final Map<Long, CreateCourseRequestDTO.AssignmentScheduleDTO> selectedScheduleMap =
                    request.getAssignmentSchedules().stream()
                            .collect(Collectors.toMap(
                                    CreateCourseRequestDTO.AssignmentScheduleDTO::getAssignmentId,
                                    dto -> dto,
                                    (existing, replacement) -> existing
                            ));

            List<CourseAssignment> courseAssignments = tutorial.getAssignments().stream()
                    .filter(assignment -> selectedScheduleMap.containsKey(assignment.getId()))
                    .map(assignment -> {
                        CreateCourseRequestDTO.AssignmentScheduleDTO schedule = selectedScheduleMap.get(assignment.getId());

                        LocalDateTime openAt = null;
                        LocalDateTime dueAt = null;

                        if (schedule.getOpenAt() != null) openAt = LocalDateTime.parse(schedule.getOpenAt());
                        if (schedule.getDueAt() != null) dueAt = LocalDateTime.parse(schedule.getDueAt());

                        return CourseAssignment.builder()
                                .courseId(savedCourse.getId())
                                .assignmentId(assignment.getId())
                                .openAt(openAt)
                                .dueAt(dueAt)
                                .build();
                    })
                    .collect(Collectors.toList());

            if (!courseAssignments.isEmpty()) {
                courseAssignmentRepository.saveAll(courseAssignments);
                assignmentsAdded = courseAssignments.size();
            }
        }

        return CreateCourseResponseDTO.builder()
                .courseId(savedCourse.getId())
                .name(savedCourse.getName())
                .code(savedCourse.getCode())
                .message("Course created successfully")
                .totalContents(contentsAdded)
                .totalAssignments(assignmentsAdded)
                .build();
    }
    public CourseRegisPublicReponse getCourseRegistrationDetailss(Long courseId) {
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
        return buildCourseRegisResponses(
                course,
                totalStudents,
                lessonsCountTotal,
                assignmentsCount
        );
    }

    private CourseRegisPublicReponse buildCourseRegisResponses(
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
        CourseRegisPublicReponse.InstructorInfo instructorInfo = CourseRegisPublicReponse.InstructorInfo.builder()
                .id(creatorId)
                .name(creator.getName())
                .email(creator.getEmail())
                .coursesCount(coursesCountByCreator) // <--- TRUYỀN GIÁ TRỊ TÍNH TOÁN
                .studentViews(totalStudentViews)     // <--- TRUYỀN GIÁ TRỊ TÍNH TOÁN
                .build();


        return CourseRegisPublicReponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .url(course.getAvatarUrl())
                .totalStudents(totalStudents)
                .lessonsCount(lessonsCountTotal)
                .totalAssignments(assignmentsCount)
                .instructor(instructorInfo)
                .build();
    }
    @Override
    public CourseRegisResponse getCourseRegistrationDetails(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Count total students
        List<Long> singleCourseIdList = List.of(courseId);
        List<Object[]> studentCountsList = enrollmentRepository.findStudentCountsByCourseIds(singleCourseIdList);
        Map<Long, Long> studentsCountMap = listToObjectMap(studentCountsList);
        Long totalStudents = studentsCountMap.getOrDefault(courseId, 0L);

        // Get contents list
        List<CourseContent> courseContents = course.getContentLinks().stream().toList();
        List<CourseRegisResponse.ContentItem> contentItems = courseContents.stream()
                .map(cc -> CourseRegisResponse.ContentItem.builder()
                        .id(cc.getContent().getId())
                        .title(cc.getContent().getTitle())
                        .orderNo(cc.getContent().getOrderNo())
                        .build())
                .sorted((a, b) -> Integer.compare(a.getOrderNo() != null ? a.getOrderNo() : 0, 
                                                   b.getOrderNo() != null ? b.getOrderNo() : 0))
                .collect(Collectors.toList());

        // Get assignments list
        List<CourseAssignment> courseAssignments = course.getAssignmentLinks().stream().toList();
        List<CourseRegisResponse.AssignmentItem> assignmentItems = courseAssignments.stream()
                .map(ca -> CourseRegisResponse.AssignmentItem.builder()
                        .id(ca.getAssignment().getId())
                        .title(ca.getAssignment().getTitle())
                        .openAt(ca.getOpenAt())
                        .dueAt(ca.getDueAt())
                        .build())
                .sorted((a, b) -> {
                    if (a.getDueAt() == null && b.getDueAt() == null) return 0;
                    if (a.getDueAt() == null) return 1;
                    if (b.getDueAt() == null) return -1;
                    return a.getDueAt().compareTo(b.getDueAt());
                })
                .collect(Collectors.toList());

        return buildCourseRegisResponse(course, totalStudents, contentItems, assignmentItems);
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
            List<CourseRegisResponse.ContentItem> contents,
            List<CourseRegisResponse.AssignmentItem> assignments
    ) {
        User creator = course.getCreator();
        Long creatorId = creator.getId();

        Long coursesCountByCreator = courseRepository.countCoursesByCreatorId(creatorId);
        Long totalStudentViews = enrollmentRepository.countTotalStudentsByCreatorId(creatorId);

        CourseRegisResponse.InstructorInfo instructorInfo = CourseRegisResponse.InstructorInfo.builder()
                .id(creatorId)
                .name(creator.getName())
                .email(creator.getEmail())
                .coursesCount(coursesCountByCreator)
                .studentViews(totalStudentViews)
                .build();

        return CourseRegisResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .totalStudents(totalStudents)
                .instructor(instructorInfo)
                .contents(contents)
                .assignments(assignments)
                .build();
    }
    @Override
    public Page<CourseItemTeacherResponse> getMyCoursesTeacher(Pageable pageable, String search) {
        long userId = Long.parseLong(authService.currentId());
        User creator = userRepository.findById(userId).orElseThrow(() ->
                new AppException(ErrorCode.NOT_FOUND));

        Page<Course> courses;
        if (search != null && !search.trim().isEmpty()) {
            courses = courseRepository.findByNameContainingIgnoreCaseAndCreator(search, creator, pageable);
        }else {
            courses = courseRepository.findByCreator(creator, pageable);
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
        List<Object[]> totalAssignmentList = assignmentRepository.findTotalAssignmentCountsPerCourse(courseIds);
        Map<Long, Long> totalAssignmentMap = totalAssignmentList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        List<Object[]> totalSubmittedAssignmentList = assignmentRepository.findSubmittedAssignmentCountsPerCourseByUser(courseIds, userId);
        Map<Long, Long> totalSubmittedAssignmentMap = totalSubmittedAssignmentList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        List<Object[]> totalLessonsList = courseContentRepository.findTotalLessonsByCourseIds(courseIds);
        Map<Long, Long> totalLessonsCountMap = totalLessonsList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // 3. Ánh xạ Page<Course> sang Page<PublicCourseItem>
        return courses.map(course -> mapToCourseItemResponseStudent(
                course,
                studentsCountMap,
                totalLessonsCountMap,
                totalAssignmentMap,
                totalSubmittedAssignmentMap
        ));
    }

    private CourseItemStudentResponse mapToCourseItemResponseStudent(
            Course course,
            Map<Long, Long> studentsCountMap,
            Map<Long, Long> totalLessonsCountMap,
            Map<Long, Long> totalAssignmentMap,
            Map<Long, Long> totalSubmittedAssignmentMap
    ) {
        Long courseId = course.getId();

        // Lấy giá trị từ Map, nếu không có thì mặc định là 0
        Long studentsCount = studentsCountMap.getOrDefault(courseId, 0L);
        Long lessonsCountTotal = totalLessonsCountMap.getOrDefault(courseId, 0L);
        Long assignmentsCount = totalAssignmentMap.getOrDefault(courseId, 0L);
        Long submittedAssignmentsCount = totalSubmittedAssignmentMap.getOrDefault(courseId, 0L);
        return CourseItemStudentResponse.builder()
                .id(courseId)
                .name(course.getName())
                .type(course.getType())
                .avatarUrl(course.getAvatarUrl())
                .currentMember(studentsCount)
                .totalLession(lessonsCountTotal)
                .lecture(CourseItemStudentResponse.Lecture.builder()
                        .id(course.getCreator().getId())
                        .name(course.getCreator().getName())
                        .avatarUrl(
                                course.getCreator().getProfile() == null ?
                                ""
                                : course.getCreator().getProfile().getAvatarUrl()
                        )
                        .build())
                .totalAssignment(assignmentsCount)
                .totalAssignmentCurrent(submittedAssignmentsCount)
                .visibility(course.getVisibility().name())
                .build();
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

    @Override
    public CreateCourseResponse createCourseFromTutorial(CreateCourseFromTutorialRequest request) {
        // 1. Validate tutorial exists và status = PUBLISHED
        Tutorial tutorial = tutorialRepository.findById(request.getTutorialId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTORIAL_NOT_EXISTED));
        
        if (tutorial.getStatus() != TutorialStatus.PUBLISHED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tutorial chưa được publish");
        }

        // 2. Get current user (creator)
        String currentUserId = authService.currentId();
        User currentUser = userRepository.findById(Long.valueOf(currentUserId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Check permission: Chỉ creator của tutorial hoặc có quyền LECTURER mới được tạo course
        // (Có thể mở rộng: nếu tutorial PUBLIC thì ai cũng tạo được)
        boolean isCreator = tutorial.getCreatedBy().equals(currentUser.getId());
        boolean hasLecturerRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_LECTURER"));
        
        if (!isCreator && !hasLecturerRole) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền tạo course từ tutorial này");
        }

        // 4. Generate course code nếu không có
        String courseCode = request.getCode();
        if (courseCode == null || courseCode.trim().isEmpty()) {
            courseCode = "COURSE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // 5. Validate course code không trùng
        if (courseRepository.existsByCode(courseCode)) {
            throw new AppException(ErrorCode.DUPLICATE, "Mã khóa học đã tồn tại");
        }

        // 6. Create Course entity
        Course course = Course.builder()
                .name(request.getName() != null ? request.getName() : tutorial.getTitle())
                .code(courseCode)
                .visibility(request.getVisibility() != null ? request.getVisibility() : CourseVisibility.PRIVATE)
                .type(request.getType())
                .avatarUrl(request.getAvatarUrl())
                .limit(request.getLimit())
                .description("Course created from tutorial: " + tutorial.getTitle())
                .creator(currentUser)
                .build();

        Course savedCourse = courseRepository.save(course);

        // 7. Link assignments from tutorial to course
        int assignmentsLinked = 0;
        if (tutorial.getAssignments() != null && !tutorial.getAssignments().isEmpty()) {
            for (Assignment assignment : tutorial.getAssignments()) {
                CourseAssignment courseAssignment = CourseAssignment.builder()
                        .courseId(savedCourse.getId())
                        .assignmentId(assignment.getId())
                        .build();
                courseAssignmentRepository.save(courseAssignment);
                assignmentsLinked++;
            }
        }

        // 8. Link contents from tutorial to course
        int contentsLinked = 0;
        if (tutorial.getContents() != null && !tutorial.getContents().isEmpty()) {
            for (Content content : tutorial.getContents()) {
                CourseContent courseContent = CourseContent.builder()
                        .courseId(savedCourse.getId())
                        .contentId(content.getId())
                        .build();
                courseContentRepository.save(courseContent);
                contentsLinked++;
            }
        }

        // 9. Create enrollment with role OWNER for creator
        Enrollment ownerEnrollment = Enrollment.builder()
                .userId(currentUser.getId())
                .courseId(savedCourse.getId())
                .role(EnrollmentRole.OWNER)
                .build();
        enrollmentRepository.save(ownerEnrollment);

        // 10. Build response
        return CreateCourseResponse.builder()
                .id(savedCourse.getId())
                .name(savedCourse.getName())
                .code(savedCourse.getCode())
                .description(savedCourse.getDescription())
                .visibility(savedCourse.getVisibility().name())
                .type(savedCourse.getType())
                .avatarUrl(savedCourse.getAvatarUrl())
                .limit(savedCourse.getLimit())
                .createdAt(savedCourse.getCreatedAt())
                .tutorialId(tutorial.getId())
                .tutorialTitle(tutorial.getTitle())
                .assignmentsCount(assignmentsLinked)
                .contentsCount(contentsLinked)
                .build();
    }

    @Override
    public JoinCourseResponse joinCourse(JoinCourseRequest request) {
        Course course;
        Long userId = Long.parseLong(authService.currentId());
        // LOGIC 1: Tìm khóa học
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            // Nếu có code -> Tìm theo code (Dành cho Private hoặc tìm nhanh)
            course = courseRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));
        } else if (request.getCourseId() != null) {
            // Nếu chỉ có ID -> Tìm theo ID
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

            // Nếu tìm bằng ID mà khóa học là PRIVATE -> Chặn lại
            if (course.getVisibility() == CourseVisibility.PRIVATE) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        } else {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        // LOGIC 2: Kiểm tra xem user đã tham gia chưa
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        long currenMember = 0;
        // LOGIC 3: Kiểm tra Slot (Giới hạn số lượng)
        if (course.getLimit() != null) {
            currenMember = enrollmentRepository.countByCourseId(course.getId());
            if (currenMember >= course.getLimit()) {
                throw new AppException(ErrorCode.LIMITED);
            }
        }

        // LOGIC 4: Lưu Enrollment
        Enrollment enrollment = Enrollment.builder()
                .userId(userId)
                .courseId(course.getId())
                .role(EnrollmentRole.STUDENT) // Mặc định là học viên
                // .joinedAt(LocalDateTime.now()) // Database đã tự handle (insertable = false)
                .build();

        enrollmentRepository.save(enrollment);

        long totalAssignment = courseAssignmentRepository.countByCourseId(course.getId());
        long totalContent = courseContentRepository.countByCourseId(course.getId());
        long totalLession = totalAssignment + totalContent;
        User createBy = course.getCreator();
        return JoinCourseResponse.builder()
                .joined(true)
                .course(
                        CourseItemStudentResponse.builder()
                                .id(course.getId())
                                .name(course.getName())
                                .type(course.getType())
                                .avatarUrl(course.getAvatarUrl())
                                .visibility(course.getVisibility().name())
                                .totalLession(totalLession)
                                .lecture(
                                        CourseItemStudentResponse.Lecture.builder()
                                                .avatarUrl(createBy.getProfile() != null ? createBy.getProfile().getAvatarUrl() : null)
                                                .id(createBy.getId())
                                                .name(createBy.getName())
                                                .build()
                                )
                                .totalAssignment(totalAssignment)
                                .totalAssignmentCurrent(0L)
                                .currentMember(currenMember + 1)
                                .build()
                )
                .build();

    }
    @Override
    @Transactional
    public CourseAvatarResponseDTO updateCourseAvatar(Long courseId, MultipartFile file) throws IOException {
        // 1. Validate file
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "File avatar không được để trống");
        }

        // 2. Get current user
        Long userId;
        try {
            userId = Long.parseLong(authService.currentId());
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Course không tồn tại"));

        // 4. Upload to Cloudinary
        String publicId = "course_" + courseId + "_" + UUID.randomUUID().toString();
        boolean success = false;
        String avatarUrl = null;

        try {
            UploadResult uploadResult = cloudinaryService.upload(file, folder + "/courses", publicId);
            avatarUrl = uploadResult.getUrl();
            success = true;

            // 6. Update course avatar URL
            course.setAvatarUrl(avatarUrl);
            courseRepository.save(course);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 7. Build response
        return CourseAvatarResponseDTO.builder()
                .courseId(courseId)
                .avatarUrl(avatarUrl)
                .success(success)
                .message("Upload avatar thành công")
                .build();
    }

    @Override
    public DetailCourseStudentResponse getCourseDetailForStudent(Long courseId) {
        // 1. Lấy thông tin User hiện tại (từ Security Context)
        // String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // User currentUser = userRepository.findByEmail(email)...;
        // Giả sử bạn đã có ID của student đang login, ví dụ:
        Long currentStudentId = 1L; // TODO: Lấy từ token/security context thực tế

        // 2. Lấy thông tin Course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 3. Mapping ContentItems
        // Dựa vào quan hệ Course -> CourseContent -> Content
        List<DetailCourseStudentResponse.ContentItem> contentItems = course.getContentLinks().stream()
                .map(link -> {
                    Content content = link.getContent();
                    return DetailCourseStudentResponse.ContentItem.builder()
                            .id(content.getId())
                            .title(content.getTitle())
                            .orderNo(content.getOrderNo())
                            // size của set mediaList
                            .totalMedia(content.getMediaList() != null ? content.getMediaList().size() : 0)
                            .build();
                })
                .sorted(Comparator.comparingInt(item ->
                        item.getOrderNo() != null ? item.getOrderNo() : Integer.MAX_VALUE))
                .collect(Collectors.toList());

        // 4. Mapping Assignments
        // Dựa vào quan hệ Course -> CourseAssignment -> Assignment
        List<DetailCourseStudentResponse.AssignmentItem> assignments = course.getAssignmentLinks().stream()
                .map(link -> {
                    // Entity CourseAssignment chứa thông tin openAt, dueAt
                    // Entity Assignment chứa thông tin title (Giả định entity Assignment có getTitle)
                    var assignmentEntity = link.getAssignment();

                    return DetailCourseStudentResponse.AssignmentItem.builder()
                            .id(assignmentEntity.getId())
                            .title(assignmentEntity.getTitle()) // TODO: Đảm bảo Assignment entity có field này
                            .openAt(link.getOpenAt())
                            .dueAt(link.getDueAt())
                            .orderNo(assignmentEntity.getOrderNo()) // CourseAssignment không có orderNo, Content mới có. Để null hoặc tự xử lý logic
                            .build();
                })
                // Sort theo ngày mở nếu không có orderNo
                .sorted(Comparator.comparing(item ->
                        item.getOpenAt() != null ? item.getOpenAt() : java.time.LocalDateTime.MAX))
                .collect(Collectors.toList());

        // 5. Tính toán Progress (Bài đã nộp / Tổng bài tập)
        int totalAssignment = assignments.size();
        long submittedCount = submissionRepository.countSubmittedAssignments(courseId, currentStudentId);

        int progressAverage = 0;
        if (totalAssignment > 0) {
            progressAverage = (int) (((double) submittedCount / totalAssignment) * 100);
        }

        // 6. Build Response
        return DetailCourseStudentResponse.builder()
                .name(course.getName())
                .description(course.getDescription())
                .totalStudent(course.getEnrollments() != null ? course.getEnrollments().size() : 0)
                .totalLession(contentItems.size() + totalAssignment)
                .totalAssignment(totalAssignment)
                .progressAverage(progressAverage)
                .contentItems(contentItems)
                .assignments(assignments)
                .build();
    }
}
