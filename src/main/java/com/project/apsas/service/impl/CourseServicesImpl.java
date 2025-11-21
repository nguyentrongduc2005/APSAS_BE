package com.project.apsas.service.impl;

import com.project.apsas.dto.request.CreateCourseFromTutorialRequest;
import com.project.apsas.dto.request.course.JoinCourseRequest;
import com.project.apsas.dto.response.*;
import com.project.apsas.dto.response.course.AssignmentResourceDTO;
import com.project.apsas.dto.response.course.ContentResourceDTO;
import com.project.apsas.dto.response.course.CourseResourceListDTO;
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
import com.project.apsas.service.CourseServices;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
//            throw new AppException(ErrorCode.BAD_REQUEST);
    @Override
    @Transactional(readOnly = true)
    public CourseResourceListDTO getAvailableResources() {
        // Lấy tất cả content có thể sử dụng
        List<ContentResourceDTO> contents = contentRepository
                .findAvailableContents();
        // Lấy tất cả assignment có thể sử dụng
        List<AssignmentResourceDTO> assignments = assignmentRepository
                .findAvailableAssignmentsForCourse();
        return CourseResourceListDTO.builder()
                .availableContents(contents)
                .availableAssignments(assignments)
                .build();
    }

    @Override
    public CreateCourseResponseDTO createCourse(CreateCourseRequestDTO request) {
        // Get current user
        String currentIdStr = authService.currentId();
        Long creatorId = Long.parseLong(currentIdStr);

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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

        // Enroll creator as LECTURER
        Enrollment creatorEnrollment = Enrollment.builder()
                .userId(creatorId)
                .courseId(savedCourse.getId())
                .role(EnrollmentRole.TEACHER)
                .build();
        enrollmentRepository.save(creatorEnrollment);

        // Add contents to course
        if (request.getContentIds() != null && !request.getContentIds().isEmpty()) {
            List<CourseContent> courseContents = request.getContentIds().stream()
                    .map(contentId -> CourseContent.builder()
                            .courseId(savedCourse.getId())
                            .contentId(contentId)
                            .build())
                    .collect(Collectors.toList());
            courseContentRepository.saveAll(courseContents);
        }

        // Add assignments to course with schedule
        if (request.getAssignments() != null && !request.getAssignments().isEmpty()) {
            List<CourseAssignment> courseAssignments = request.getAssignments().stream()
                    .map(assignmentSchedule -> {
                        LocalDateTime openAt = null;
                        LocalDateTime dueAt = null;

                        if (assignmentSchedule.getOpenAt() != null) {
                            openAt = LocalDateTime.parse(assignmentSchedule.getOpenAt());
                        }
                        if (assignmentSchedule.getDueAt() != null) {
                            dueAt = LocalDateTime.parse(assignmentSchedule.getDueAt());
                        }

                        return CourseAssignment.builder()
                                .courseId(savedCourse.getId())
                                .assignmentId(assignmentSchedule.getAssignmentId())
                                .openAt(openAt)
                                .dueAt(dueAt)
                                .build();
                    })
                    .collect(Collectors.toList());
            courseAssignmentRepository.saveAll(courseAssignments);
        }

        return CreateCourseResponseDTO.builder()
                .courseId(savedCourse.getId())
                .name(savedCourse.getName())
                .code(savedCourse.getCode())
                .message("Course created successfully")
                .totalContents(request.getContentIds() != null ? request.getContentIds().size() : 0)
                .totalAssignments(request.getAssignments() != null ? request.getAssignments().size() : 0)
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
        List<CourseContent> courseContents = course.getCourseContents().stream().toList();
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
        List<CourseAssignment> courseAssignments = course.getCourseAssignments().stream().toList();
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
                                                .avatarUrl(createBy.getProfile().getAvatarUrl())
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
}
