-- Vô hiệu hóa kiểm tra khóa ngoại để chèn dữ liệu
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- ========================================================
-- 1. TẠO USERS VÀ PROFILES (Bổ sung cho DataSeeder)
-- DataSeeder đã tạo user admin (ID 1)
-- ========================================================

INSERT INTO `users` (`name`, `email`, `password`, `status`, `created_at`) VALUES
    ('Lê Văn Giáo Viên', 'teacher@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE', NOW());
SET @teacher_id = LAST_INSERT_ID(); -- Lưu ID của Teacher

INSERT INTO `users` (`name`, `email`, `password`, `status`, `created_at`) VALUES
    ('Nguyễn Văn Học Sinh', 'student@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE', NOW());
SET @student_id = LAST_INSERT_ID(); -- Lưu ID của Student

-- Gán vai trò cho user mới, dựa trên Roles mà DataSeeder đã tạo
INSERT INTO `users_roles` (`users_id`, `roles_id`) VALUES
                                                       (@teacher_id, (SELECT id FROM roles WHERE name = 'LECTURER')),
                                                       (@student_id, (SELECT id FROM roles WHERE name = 'STUDENT'));

INSERT INTO `profiles` (`user_id`, `avatar_url`, `dob`, `gender`, `phone`, `bio`) VALUES
                                                                                      (@teacher_id, 'https://i.pravatar.cc/300?u=teacher', '1990-05-15', 'MALE', '0905123456', 'Giảng viên mười năm kinh nghiệm về Java và Spring Boot.'),
                                                                                      (@student_id, 'https://i.pravatar.cc/300?u=student', '2003-10-20', 'MALE', '0912789101', 'Em là sinh viên năm 3, đam mê lập trình backend.');

-- ========================================================
-- 2. TẠO KỸ NĂNG (SKILLS)
-- ========================================================

INSERT INTO `skills` (`name`, `description`, `category`, `created_by`) VALUES
                                                                           ('Java Core', 'Kiến thức cốt lõi về ngôn ngữ Java', 'OTHER', 1),
                                                                           ('Spring Boot', 'Xây dựng ứng dụng với Spring Boot', 'OTHER', 1),
                                                                           ('JPA/Hibernate', 'Làm việc với CSDL qua JPA', 'OTHER', 1),
                                                                           ('Mảng (Array)', 'Kỹ thuật xử lý mảng', 'ARRAY', 1);
SET @skill_array_id = LAST_INSERT_ID(); -- Lưu ID của skill "Mảng (Array)"

-- ========================================================
-- 3. TẠO KHÓA HỌC VÀ GHI DANH
-- ========================================================

INSERT INTO `courses` (`name`, `code`, `visibility`, `limit`, `type`, `avatar_url`) VALUES
    ('Lập Trình Backend với Spring Boot', 'SPB_2025', 'PUBLIC', 100, 'Chuyên ngành', 'https://images.unsplash.com/photo-1607703703578-508b1fadf879?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3wxMTc4M3wwfDF8c2VhcmNofDEzfHxKYXZhfGVufDB8fHx8MTY5ODc0OTM0OHww&ixlib=rb-4.0.3&q=80&w=1080');
SET @course_spring_id = LAST_INSERT_ID();

INSERT INTO `courses` (`name`, `code`, `visibility`, `limit`, `type`, `avatar_url`) VALUES
    ('Lập Trình Frontend với React', 'REACT_2025', 'PRIVATE', 50, 'Tự chọn', 'https://images.unsplash.com/photo-1633356122102-3fe601e05bd2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3wxMTc4M3wwfDF8c2VhcmNofDF8fFJlYWN0fGVufDB8fHx8MTY5ODc0OTM4M3ww&ixlib=rb-4.0.3&q=80&w=1080');
SET @course_react_id = LAST_INSERT_ID();

INSERT INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`) VALUES
                                                                            (1, @course_spring_id, 'OWNER', NOW()),    -- Admin 1 (từ DataSeeder) là Owner khóa Spring
                                                                            (@teacher_id, @course_spring_id, 'TEACHER', NOW()),  -- Teacher là Teacher khóa Spring
                                                                            (@student_id, @course_spring_id, 'STUDENT', NOW()),  -- Student là Student khóa Spring
                                                                            (@student_id, @course_react_id, 'STUDENT', NOW());  -- Student cũng học khóa React

-- ========================================================
-- 4. TẠO NỘI DUNG (TUTORIALS, CONTENTS)
-- ========================================================

INSERT INTO `tutorials` (`created_by`, `title`, `summary`, `status`) VALUES
    (@teacher_id, 'Hướng Dẫn Spring Boot Cơ Bản', 'Bao gồm các khái niệm về DI, IoC, và REST API', 'PUBLISHED'); -- Tạo bởi Teacher
SET @tutorial_id = LAST_INSERT_ID();

INSERT INTO `contents` (`tutorial_id`, `title`, `body_md`, `order_no`, `status`) VALUES
    (@tutorial_id, 'Chương 1: Giới Thiệu Spring Boot', 'Spring Boot là gì? Tại sao...', 1, 'PUBLISHED');
SET @content_id = LAST_INSERT_ID();

INSERT INTO `contents` (`tutorial_id`, `title`, `body_md`, `order_no`, `status`) VALUES
    (@tutorial_id, 'Chương 2: Dependency Injection', 'DI và IoC là cốt lõi của Spring...', 2, 'PUBLISHED');

INSERT INTO `media` (`content_id`, `type`, `url`, `caption`, `order_no`) VALUES
    (@content_id, 'IMAGE', 'https://i.imgur.com/vBLHq1F.png', 'Logo Spring Boot', 1);

INSERT INTO `courses_contents` (`courses_id`, `contents_id`) VALUES
                                                                 (@course_spring_id, @content_id), -- Gán "Chương 1" vào khóa Spring
                                                                 (@course_spring_id, @content_id + 1); -- Gán "Chương 2" vào khóa Spring

-- ========================================================
-- 5. TẠO BÀI TẬP VÀ BÀI NỘP (ASSIGNMENTS & SUBMISSIONS)
-- ========================================================

INSERT INTO `assignments` (`tutorial_id`, `skill_id`, `title`, `statement_md`, `max_score`, `attempts_limit`) VALUES
    (@tutorial_id, @skill_array_id, 'Bài toán Two Sum', 'Cho một mảng số nguyên `nums` và một số nguyên `target`, trả về chỉ số của hai số sao cho tổng của chúng bằng `target`...', 100.00, 10);
SET @assignment_id = LAST_INSERT_ID();

INSERT INTO `courses_assignments` (`courses_id`, `assignments_id`, `open_at`, `due_at`) VALUES
    (@course_spring_id, @assignment_id, NOW(), '2025-12-31 23:59:59'); -- Gán bài "Two Sum" cho khóa Spring

INSERT INTO `assignment_evaluations` (`name`, `type`, `config_json`) VALUES
    ('Chấm test case tự động', 'JUDGE', '{"runner": "java_junit", "timeout_ms": 5000, "compare_mode": "STRICT"}');
SET @eval_id = LAST_INSERT_ID();

INSERT INTO `assignment_evaluation_maps` (`assignment_id`, `evaluation_id`, `weight`) VALUES
    (@assignment_id, @eval_id, 100.00); -- Bài Two Sum dùng 100% điểm từ "Chấm test case tự động"

-- Đây là bài nộp mẫu (PENDING) để test luồng Kafka của bạn
INSERT INTO `submissions` (
    `assignment_id`, `user_id`, `language`, `code`,
    `status`, `attempt_no`, `submitted_at`
) VALUES (
             @assignment_id, @student_id, 'java',
             'public class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Đây là code đang PENDING\n        return new int[0];\n    }\n}',
             'PENDING', 1, NOW()
         );

-- Đây là bài nộp đã HOÀN THÀNH (COMPLETE) để test hiển thị
INSERT INTO `submissions` (
    `assignment_id`, `user_id`, `language`, `code`,
    `report_json`, `score`, `status`, `suggestion`,
    `big_o_complexity_time`, `big_o_complexity_space`, `feedback`, `passed`,
    `attempt_no`, `submitted_at`
) VALUES (
             @assignment_id, @student_id, 'java',
             'public class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < nums.length; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                return new int[] { map.get(complement), i };\n            }\n            map.put(nums[i], i);\n        }\n        return new int[0];\n    }\n}',
             '{"test_cases_total": 10, "test_cases_passed": 10, "runtime_ms": 3, "memory_kb": 42100}',
             100.00, 'COMPLETE',
             'Giải pháp của bạn rất tối ưu. Không có gợi ý nào thêm.',
             'O(n)', 'O(n)',
             'Bạn đã sử dụng HashMap một cách hiệu quả để giải bài toán trong một lượt duyệt.',
             1, 2, DATE_ADD(NOW(), INTERVAL 5 MINUTE)
         );
SET @submission_complete_id = LAST_INSERT_ID();


-- ========================================================
-- 6. TẠO DỮ LIỆU PHỤ (FEEDBACK, HELP, NOTIFICATIONS)
-- ========================================================

-- Feedback của Teacher (hoặc hệ thống) cho Bài nộp đã Hoàn thành
INSERT INTO `feedback` (`body`, `created_at`, `submission_id`) VALUES
    ('Code của bạn rất sạch và hiệu quả. Tốt lắm!', NOW(), @submission_complete_id);

-- Yêu cầu trợ giúp từ Student
INSERT INTO `help_requests` (`user_id`, `course_id`, `title`, `body`) VALUES
    (@student_id, @course_spring_id, 'Em không hiểu về @Autowired', 'Thầy có thể giải thích sự khác nhau giữa @Autowired và constructor injection không ạ?');

-- Thông báo cho Student
INSERT INTO `notifications` (`user_id`, `type`, `payload`, `is_read`) VALUES
    (@student_id, 'NEW_ASSIGNMENT', '{"assignment_title": "Bài toán Two Sum", "course_name": "Lập Trình Backend với Spring Boot"}', 0);

-- Tiến độ (Progress) của Student
INSERT INTO `progress` (`user_id`, `total_attempt_no`, `acceptance`) VALUES
    (@student_id, 2, 0.5); -- 2 lần nộp, 1 lần pass (0.5)
SET @progress_id = LAST_INSERT_ID();

INSERT INTO `progress_skills` (`progress_id`, `skill_id`, `level`, `score`) VALUES
    (@progress_id, @skill_array_id, 3, 100.00); -- Student đạt level 3, 100 điểm cho skill "Mảng (Array)"

-- Kích hoạt lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS=1;
COMMIT;