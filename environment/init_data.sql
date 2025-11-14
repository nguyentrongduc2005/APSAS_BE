-- Vô hiệu hóa kiểm tra khóa ngoại để chèn dữ liệu
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- === SỬA LỖI 1046: CHỌN DATABASE ĐỂ LÀM VIỆC ===
USE apsas_db;
-- =============================================

-- ========================================================
-- 1. TẠO ROLES VÀ USERS (LECTURER, PROVIDER, 30 STUDENTS)
-- ========================================================

INSERT IGNORE INTO `roles` (`name`, `description`) VALUES
    ('LECTURER', 'Giảng viên, có thể tạo khóa học và quản lý nội dung'),
    ('STUDENT', 'Học sinh, có thể tham gia khóa học và nộp bài'),
    ('ADMIN', 'Quản trị viên hệ thống'),
    ('PROVIDER', 'Người cung cấp hướng dẫn (tutorials)');

-- Tạo User LECTURER (Giảng viên)
INSERT INTO `users` (`name`, `email`, `password`, `status`) VALUES
    ('Lê Văn Giảng Viên', 'lecturer@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');
SET @lecturer_id = LAST_INSERT_ID();

-- Tạo User PROVIDER (Người tạo tutorial)
INSERT INTO `users` (`name`, `email`, `password`, `status`) VALUES
    ('Nguyễn Thị Cung Cấp', 'provider@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');
SET @provider_id = LAST_INSERT_ID();

-- Gán vai trò
INSERT INTO `users_roles` (`users_id`, `roles_id`) VALUES
                                                       (@lecturer_id, (SELECT id FROM roles WHERE name = 'LECTURER')),
                                                       (@provider_id, (SELECT id FROM roles WHERE name = 'PROVIDER'));

-- Tạo Profiles cho 2 user trên
INSERT INTO `profiles` (`user_id`, `avatar_url`, `bio`) VALUES
                                                            (@lecturer_id, 'https://i.pravatar.cc/300?u=lecturer', 'Giảng viên 10 năm kinh nghiệm Java Spring.'),
                                                            (@provider_id, 'https://i.pravatar.cc/300?u=provider', 'Chuyên gia tạo nội dung và hướng dẫn lập trình.');

-- Tạo 30 STUDENT Users bằng Recursive CTE
INSERT INTO `users` (`name`, `email`, `password`, `status`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
    CONCAT('Học Sinh ', n),
    CONCAT('student_', n, '@apsas.edu.vn'),
    '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', -- Mật khẩu chung
    'ACTIVE'
FROM seq;

-- Gán vai trò STUDENT cho 30 user vừa tạo
SET @role_student_id = (SELECT id FROM roles WHERE name = 'STUDENT');
INSERT INTO `users_roles` (`users_id`, `roles_id`)
SELECT
    u.id,
    @role_student_id
FROM `users` u
WHERE u.email LIKE 'student_%@apsas.edu.vn';

-- Tạo 30 Profiles cho 30 student
INSERT INTO `profiles` (`user_id`, `avatar_url`, `bio`)
SELECT
    u.id,
    CONCAT('https://i.pravatar.cc/300?u=student', u.id),
    'Sinh viên chăm chỉ, đam mê lập trình.'
FROM `users` u
WHERE u.email LIKE 'student_%@apsas.edu.vn';

-- ========================================================
-- 2. TẠO KỸ NĂNG (SKILLS)
-- ========================================================

INSERT INTO `skills` (`name`, `description`, `category`, `created_by`) VALUES
                                                                           ('Java Core', 'Kiến thức cốt lõi về ngôn ngữ Java', 'OTHER', @provider_id),
                                                                           ('Spring Boot', 'Xây dựng ứng dụng với Spring Boot', 'OTHER', @provider_id),
                                                                           ('JPA/Hibernate', 'Làm việc với CSDL qua JPA', 'OTHER', @provider_id),
                                                                           ('Mảng (Array)', 'Kỹ thuật xử lý mảng', 'ARRAY', @provider_id),
                                                                           ('Quy hoạch động', 'Dynamic Programming', 'DYNAMIC_PROGRAMMING', @provider_id),
                                                                           ('Lý thuyết đồ thị', 'Graph Theory', 'GRAPH_ALGORITHM', @provider_id),
                                                                           ('Xử lý chuỗi', 'String Manipulation', 'STRING', @provider_id);

SET @skill_array_id = (SELECT id FROM skills WHERE name = 'Mảng (Array)');
SET @skill_dp_id = (SELECT id FROM skills WHERE name = 'Quy hoạch động');


-- ========================================================
-- 3. TẠO 10 TUTORIALS (Bởi Provider)
-- ========================================================

INSERT INTO `tutorials` (`created_by`, `title`, `summary`, `status`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10
)
SELECT
    @provider_id,
    CONCAT('Tutorial ', n, ': Kỹ Thuật Lập Trình'),
    CONCAT('Tóm tắt nội dung cho Tutorial ', n),
    'PUBLISHED'
FROM seq;

-- ========================================================
-- 4. TẠO 10 KHÓA HỌC (Bởi Lecturer)
-- ========================================================

INSERT INTO `courses` (`name`, `code`, `visibility`, `type`, `avatar_url`, `created_by`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10
)
SELECT
    CONCAT('Khóa học Lập Trình ', n),
    CONCAT('COURSE_', n),
    'PUBLIC',
    'Chuyên ngành',
    CONCAT('https://images.unsplash.com/photo-1607703703578-508b1fadf879?w=400&q=80&n=', n),
    @lecturer_id
FROM seq;


-- ========================================================
-- 5. GHI DANH (Enrollments)
-- ========================================================

-- Ghi danh LECTURER làm OWNER cho 10 khóa học của mình
INSERT INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`)
SELECT
    @lecturer_id,
    c.id,
    'OWNER',
    NOW()
FROM `courses` c
WHERE c.created_by = @lecturer_id;

-- Ghi danh 30 STUDENTS vào 10 KHÓA HỌC (30 * 10 = 300 lượt ghi danh)
INSERT INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`)
SELECT
    u.id,
    c.id,
    'STUDENT',
    NOW()
FROM
    `users` u
        CROSS JOIN
    `courses` c
WHERE
    u.email LIKE 'student_%@apsas.edu.vn'
  AND c.created_by = @lecturer_id;


-- ========================================================
-- 6. TẠO NỘI DUNG (10 Contents cho mỗi Tutorial = 100 Contents)
-- ========================================================

INSERT INTO `contents` (`tutorial_id`, `title`, `body_md`, `order_no`, `status`)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 10
)
SELECT
    t.id,
    CONCAT('Chương ', nums.n, ' của ', t.title),
    CONCAT('Nội dung chi tiết cho Chương ', nums.n, '...'),
    nums.n, -- order_no đã có ở đây
    'PUBLISHED'
FROM
    `tutorials` t
        CROSS JOIN
    nums
WHERE
    t.created_by = @provider_id;

-- ========================================================
-- 7. TẠO BÀI TẬP (10 Assignments cho mỗi Tutorial = 100 Assignments)
-- ========================================================

INSERT INTO `assignments` (
    `tutorial_id`, `skill_id`, `title`, `statement_md`, `max_score`, `attempts_limit`,
    `order_no`, `proficiency`
)
WITH RECURSIVE nums AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 10
)
SELECT
    t.id,
    -- Gán skill xen kẽ cho đa dạng
    CASE WHEN nums.n % 2 = 0 THEN @skill_array_id ELSE @skill_dp_id END,
    CONCAT('Bài tập ', nums.n, ' của ', t.title),
    CONCAT('Đề bài chi tiết cho Bài tập ', nums.n, '...'),
    100.00,
    10,
    nums.n, -- Gán order_no (từ 1 đến 10)
    CASE FLOOR(RAND() * 3) -- Gán proficiency ngẫu nhiên
        WHEN 0 THEN 'Dễ'
        WHEN 1 THEN 'Trung bình'
        ELSE 'Khó'
        END
FROM
    `tutorials` t
        CROSS JOIN
    nums
WHERE
    t.created_by = @provider_id;


-- ========================================================
-- 8. ÁNH XẠ KHÓA HỌC VỚI NỘI DUNG (1-1 Course-Tutorial)
-- ========================================================

INSERT INTO `courses_contents` (`courses_id`, `contents_id`)
WITH Tuts AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM `tutorials`
    WHERE created_by = @provider_id
), Cors AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM `courses`
    WHERE created_by = @lecturer_id
)
SELECT
    Cors.id, -- ID của Course
    Cont.id  -- ID của Content
FROM Tuts
         JOIN Cors ON Tuts.rn = Cors.rn -- Ghép Tutorial_N với Course_N
         JOIN `contents` Cont ON Cont.tutorial_id = Tuts.id; -- Lấy tất cả content thuộc Tutorial_N


INSERT INTO `courses_assignments` (`courses_id`, `assignments_id`, `open_at`, `due_at`)
WITH Tuts AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM `tutorials`
    WHERE created_by = @provider_id
), Cors AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM `courses`
    WHERE created_by = @lecturer_id
)
SELECT
    Cors.id, -- ID của Course
    Assi.id, -- ID của Assignment
    NOW(),
    '2025-12-31 23:59:59'
FROM Tuts
         JOIN Cors ON Tuts.rn = Cors.rn -- Ghép Tutorial_N với Course_N
         JOIN `assignments` Assi ON Assi.tutorial_id = Tuts.id; -- Lấy tất cả assignment thuộc Tutorial_N


-- ========================================================
-- 9. TẠO CẤU HÌNH CHẤM BÀI (Assignment Evaluations)
-- ========================================================

INSERT INTO `assignment_evaluations` (
    `assignment_id`,
    `name`,
    `type`,
    `config_json`
)
SELECT
    a.id,
    'Chấm test case tự động',
    'JUDGE',
    -- Cấu trúc JSON theo yêu cầu (ConfigJson -> List<TestCase>)
    '{
        "testCase": [
            {
                "in": "1 2",
                "out": "3",
                "visibility": "PUBLIC"
            },
            {
                "in": "5 5",
                "out": "10",
                "visibility": "HIDDEN"
            },
            {
                "in": "-1 -5",
                "out": "-6",
                "visibility": "HIDDEN"
            }
        ]
    }'
FROM `assignments` a
WHERE a.tutorial_id IN (SELECT id FROM tutorials WHERE created_by = @provider_id);


-- ========================================================
-- 10. TẠO DỮ LIỆU MẪU (Submissions, Feedback...)
-- ========================================================

-- Lấy ID của 1 student và 1 assignment bất kỳ để làm mẫu
SET @sample_student_id = (SELECT id FROM users WHERE email = 'student_1@apsas.edu.vn' LIMIT 1);
SET @sample_assignment_id = (SELECT id FROM assignments ORDER BY id ASC LIMIT 1);
SET @sample_course_id = (SELECT courses_id FROM courses_assignments WHERE assignments_id = @sample_assignment_id LIMIT 1);

-- Bài nộp PENDING (đang chờ chấm)
INSERT INTO `submissions` (
    `assignment_id`, `course_id`, `user_id`, `language`, `code`, `status`, `attempt_no`, `submitted_at`
) VALUES (
             @sample_assignment_id,
             @sample_course_id, -- ĐÃ THÊM course_id
             @sample_student_id,
             'java',
             'public class Solution { ... // Code đang chờ chấm }',
             'PENDING', 1, NOW()
         );

-- Bài nộp COMPLETE (đã chấm xong)
INSERT INTO `submissions` (
    `assignment_id`, `course_id`, `user_id`, `language`, `code`,
    `report_json`, `score`, `status`, `suggestion`,
    `big_o_complexity_time`, `big_o_complexity_space`, `feedback`, `passed`,
    `attempt_no`, `submitted_at`
) VALUES (
             @sample_assignment_id,
             @sample_course_id, -- ĐÃ THÊM course_id
             @sample_student_id,
             'java',
             'public class Solution { ... // Code tối ưu }',
             -- Cấu trúc JSON theo yêuCầu (ReportCongfigSubmission -> List<TestCaseResult>)
             '{
                 "averageTime": 120.5,
                 "averageMemory": 4096.0,
                 "totalTestCases": 3,
                 "passedTestCases": 3,
                 "testCases": [
                     {
                         "status": "Accepted",
                         "time": 110.0,
                         "memory": 4090,
                         "visibility": "PUBLIC",
                         "stdin": "1 2",
                         "stdout": "3",
                         "expectedOutput": "3"
                     },
                     {
                         "status": "Accepted",
                         "time": 120.0,
                         "memory": 4100,
                         "visibility": "PRIVATE",
                         "stdin": "5 5",
                         "stdout": "10",
                         "expectedOutput": "10"
                     },
                     {
                         "status": "Accepted",
                         "time": 131.5,
                         "memory": 4098,
                         "visibility": "PRIVATE",
                         "stdin": "-1 -5",
                         "stdout": "-6",
                         "expectedOutput": "-6"
                     }
                 ]
             }',
             100.00, 'COMPLETE',
             'Giải pháp của bạn rất tối ưu.',
             'O(n)', 'O(n)',
             'Bạn đã sử dụng HashMap hiệu quả.',
             1, 2, DATE_ADD(NOW(), INTERVAL 5 MINUTE)
         );
SET @submission_complete_id = LAST_INSERT_ID();

-- Feedback của Giảng viên cho bài nộp
INSERT INTO `feedback` (`body`, `created_at`, `submission_id`) VALUES
    ('Code của bạn rất tốt, đúng theo yêu cầu!', NOW(), @submission_complete_id);

-- Yêu cầu trợ giúp từ Student
INSERT INTO `help_requests` (`user_id`, `course_id`, `title`, `body`) VALUES
    (@sample_student_id, @sample_course_id, 'Em không hiểu về Quy hoạch động', 'Thầy/cô có thể giải thích bài tập này theo DP không ạ?');

-- Thông báo cho Student
INSERT INTO `notifications` (`user_id`, `type`, `payload`, `is_read`) VALUES
    (@sample_student_id, 'NEW_ASSIGNMENT', '{"assignment_title": "Bài tập 1", "course_name": "Khóa học Lập Trình 1"}', 0);

-- Tiến độ (Progress) của Student
INSERT INTO `progress` (`user_id`, `total_attempt_no`, `acceptance`) VALUES
    (@sample_student_id, 2, 0.5); -- 2 lần nộp, 1 lần pass (0.5)
SET @progress_id = LAST_INSERT_ID();

INSERT INTO `progress_skills` (`progress_id`, `skill_id`, `level`, `score`) VALUES
    (@progress_id, @skill_array_id, 1, 100.00); -- Giả sử bài nộp thuộc skill_array_id


-- Kích hoạt lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS=1;
COMMIT;