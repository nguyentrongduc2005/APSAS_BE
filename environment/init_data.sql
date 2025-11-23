-- =============================================
-- CONFIGURATION
-- =============================================
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

USE apsas_db;

-- ========================================================
-- 1. ĐỒNG BỘ ROLES (Dựa trên Java DataSeeder)
-- ========================================================
-- Java dùng: ADMIN, LECTURER, STUDENT, CONTENT_PROVIDER, GUEST
-- Chúng ta chỉ insert nếu chưa tồn tại (để tránh lỗi Duplicate Entry)

INSERT IGNORE INTO `roles` (`name`, `description`) VALUES
('ADMIN', 'Quản trị viên hệ thống'),
('LECTURER', 'Giảng viên'),
('STUDENT', 'Học viên'),
('CONTENT_PROVIDER', 'Người cung cấp nội dung'), -- Đổi PROVIDER -> CONTENT_PROVIDER khớp với Java
('GUEST', 'Khách vãng lai');

-- LẤY ID CỦA CÁC ROLE VÀO BIẾN (Quan trọng: Không dùng ID cứng)
SET @role_admin_id   = (SELECT id FROM roles WHERE name = 'ADMIN' LIMIT 1);
SET @role_lecturer_id = (SELECT id FROM roles WHERE name = 'LECTURER' LIMIT 1);
SET @role_student_id  = (SELECT id FROM roles WHERE name = 'STUDENT' LIMIT 1);
SET @role_provider_id = (SELECT id FROM roles WHERE name = 'CONTENT_PROVIDER' LIMIT 1);

-- ========================================================
-- 2. BỔ SUNG PERMISSIONS (Nếu Java chưa có)
-- ========================================================
-- Java tạo các permission dạng: COURSE_READ, COURSE_CREATE...
-- Script cũ của bạn dùng: CREATE_COURSE, VIEW_COURSES...
-- -> Ta insert thêm các permission cũ của bạn vào để logic cũ hoạt động, dùng INSERT IGNORE

INSERT IGNORE INTO permissions (name, description) VALUES
-- User & Role
('MANAGE_USERS', 'Full access to manage users'),
('MANAGE_ROLES', 'Full access to manage roles'),
-- Tutorial (Provider)
('MANAGE_TUTORIALS', 'Full access to manage all tutorials'),
('PUBLISH_TUTORIALS', 'Approve and publish tutorials'),
('CREATE_TUTORIAL', 'Create new tutorials'),
('UPDATE_TUTORIAL', 'Update own tutorials'),
('DELETE_TUTORIAL', 'Delete own tutorials'),
('VIEW_OWN_TUTORIALS', 'View own tutorial list'),
-- Content & Assignment
('CREATE_CONTENT', 'Create content'),
('CREATE_ASSIGNMENT', 'Create assignments'),
-- Course (Lecturer/Student)
('ENROLL_COURSE', 'Enroll in courses'),
('VIEW_COURSES', 'View enrolled courses'),
('SUBMIT_ASSIGNMENT', 'Submit assignments'),
('VIEW_SUBMISSIONS', 'View student submissions'),
('EVALUATE_SUBMISSIONS', 'Evaluate submissions'),
('VIEW_FEEDBACK', 'View feedback'),
('RESPOND_FEEDBACK', 'Respond to feedback'),
('REQUEST_HELP', 'Request help');

-- Gán thêm permission cho Role (Map theo tên permission và biến Role ID)
-- Ví dụ: Gán quyền cũ cho LECTURER
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_lecturer_id, id FROM permissions WHERE name IN (
                                                             'VIEW_SUBMISSIONS', 'EVALUATE_SUBMISSIONS', 'VIEW_FEEDBACK', 'RESPOND_FEEDBACK',
                                                             'CREATE_COURSE', 'UPDATE_COURSE', 'DELETE_COURSE' -- Lưu ý: Java dùng COURSE_CREATE, ở đây ta cứ map cái cũ cho chắc
    );

-- Gán quyền cho STUDENT
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_student_id, id FROM permissions WHERE name IN (
                                                            'SUBMIT_ASSIGNMENT', 'ENROLL_COURSE', 'VIEW_COURSES', 'REQUEST_HELP'
    );

-- Gán quyền cho CONTENT_PROVIDER
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_provider_id, id FROM permissions WHERE name IN (
                                                             'CREATE_TUTORIAL', 'UPDATE_TUTORIAL', 'DELETE_TUTORIAL', 'VIEW_OWN_TUTORIALS',
                                                             'CREATE_CONTENT', 'CREATE_ASSIGNMENT'
    );

-- ========================================================
-- 3. TẠO USERS (LECTURER, PROVIDER)
-- ========================================================

-- Tạo User LECTURER (Nếu chưa có)
INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`) VALUES
('Lê Văn Giảng Viên', 'lecturer@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');

-- Lấy ID user vừa tạo (hoặc user cũ nếu đã có)
SET @lecturer_id = (SELECT id FROM users WHERE email = 'lecturer@apsas.edu.vn' LIMIT 1);

-- Tạo User PROVIDER
INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`) VALUES
('Nguyễn Thị Cung Cấp', 'provider@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');

SET @provider_id = (SELECT id FROM users WHERE email = 'provider@apsas.edu.vn' LIMIT 1);

-- Gán Role cho Users (Dùng INSERT IGNORE để tránh duplicate)
INSERT IGNORE INTO `users_roles` (`users_id`, `roles_id`) VALUES
(@lecturer_id, @role_lecturer_id),
(@provider_id, @role_provider_id);

-- Tạo Profile (Dùng ON DUPLICATE KEY UPDATE để không lỗi nếu chạy lại)
INSERT INTO `profiles` (`user_id`, `avatar_url`, `bio`) VALUES
                                                            (@lecturer_id, 'https://i.pravatar.cc/300?u=lecturer', 'Giảng viên 10 năm kinh nghiệm Java Spring.'),
                                                            (@provider_id, 'https://i.pravatar.cc/300?u=provider', 'Chuyên gia tạo nội dung và hướng dẫn lập trình.')
    ON DUPLICATE KEY UPDATE bio = VALUES(bio);

-- ========================================================
-- 4. TẠO 30 STUDENTS
-- ========================================================

INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
    CONCAT('Học Sinh ', n),
    CONCAT('student_', n, '@apsas.edu.vn'),
    '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa',
    'ACTIVE'
FROM seq;

-- Gán Role STUDENT cho các user có email student_
INSERT IGNORE INTO `users_roles` (`users_id`, `roles_id`)
SELECT u.id, @role_student_id
FROM `users` u
WHERE u.email LIKE 'student_%@apsas.edu.vn';

-- Tạo Profile cho student
INSERT IGNORE INTO `profiles` (`user_id`, `avatar_url`, `bio`)
SELECT
    u.id,
    CONCAT('https://i.pravatar.cc/300?u=student', u.id),
    'Sinh viên chăm chỉ.'
FROM `users` u
WHERE u.email LIKE 'student_%@apsas.edu.vn';

-- ========================================================
-- 5. TẠO KỸ NĂNG & TUTORIALS (Dùng @provider_id)
-- ========================================================

INSERT IGNORE INTO `skills` (`name`, `description`, `category`, `created_by`) VALUES
('Java Core', 'Kiến thức cốt lõi', 'OTHER', @provider_id),
('Mảng (Array)', 'Kỹ thuật xử lý mảng', 'ARRAY', @provider_id),
('Quy hoạch động', 'Dynamic Programming', 'DYNAMIC_PROGRAMMING', @provider_id);

SET @skill_array_id = (SELECT id FROM skills WHERE name = 'Mảng (Array)' LIMIT 1);
SET @skill_dp_id = (SELECT id FROM skills WHERE name = 'Quy hoạch động' LIMIT 1);

-- Tạo Tutorials
INSERT IGNORE INTO `tutorials` (`created_by`, `title`, `summary`, `status`)
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
-- 6. TẠO COURSES (Dùng @lecturer_id)
-- ========================================================

INSERT IGNORE INTO `courses` (`name`, `code`, `visibility`, `type`, `avatar_url`, `created_by`)
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
    CONCAT('https://images.unsplash.com/photo-1607703703578?n=', n),
    @lecturer_id
FROM seq;

-- ========================================================
-- 7. ENROLLMENTS & CONTENTS
-- ========================================================

-- Lecturer enroll chính khóa của mình
INSERT IGNORE INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`)
SELECT @lecturer_id, c.id, 'OWNER', NOW()
FROM `courses` c WHERE c.created_by = @lecturer_id;

-- Students enroll
INSERT IGNORE INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`)
SELECT u.id, c.id, 'STUDENT', NOW()
FROM `users` u CROSS JOIN `courses` c
WHERE u.email LIKE 'student_%@apsas.edu.vn' AND c.created_by = @lecturer_id;

-- Tạo Contents
INSERT IGNORE INTO `contents` (`tutorial_id`, `title`, `body_md`, `order_no`, `status`)
WITH RECURSIVE nums AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM nums WHERE n < 10)
SELECT
    t.id,
    CONCAT('Chương ', nums.n, ' của ', t.title),
    'Nội dung chi tiết...',
    nums.n,
    'PUBLISHED'
FROM `tutorials` t CROSS JOIN nums
WHERE t.created_by = @provider_id;

-- ========================================================
-- 8. ASSIGNMENTS (Dữ liệu mẫu)
-- ========================================================

-- Lưu ý: Đã sửa proficiency thành int theo yêu cầu
INSERT IGNORE INTO `assignments` (
    `tutorial_id`, `skill_id`, `title`, `statement_md`, `max_score`,
    `attempts_limit`, `order_no`, `proficiency`
)
WITH RECURSIVE nums AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM nums WHERE n < 10)
SELECT
    t.id,
    CASE WHEN nums.n % 2 = 0 THEN @skill_array_id ELSE @skill_dp_id END,
    CONCAT('Bài tập ', nums.n, ' của ', t.title),
    'Đề bài chi tiết...',
    100.00,
    10,
    nums.n,
    FLOOR(RAND() * 3)
FROM `tutorials` t CROSS JOIN nums
WHERE t.created_by = @provider_id;

-- Link Course - Content (Map đại diện)
INSERT IGNORE INTO `courses_contents` (`courses_id`, `contents_id`)
SELECT c.id, ct.id
FROM courses c JOIN tutorials t ON t.created_by = @provider_id -- Map lỏng để demo
               JOIN contents ct ON ct.tutorial_id = t.id
WHERE c.created_by = @lecturer_id LIMIT 50;

-- ========================================================
-- 9. DỮ LIỆU CHẤM BÀI MẪU (Submission)
-- ========================================================
SET @s1_id = (SELECT id FROM users WHERE email = 'student_1@apsas.edu.vn' LIMIT 1);
SET @asm_id = (SELECT id FROM assignments ORDER BY id ASC LIMIT 1);
-- Tìm course chứa assignment này (giả định logic link ở trên)
SET @c_id = (SELECT id FROM courses ORDER BY id ASC LIMIT 1);

INSERT IGNORE INTO `submissions` (
    `assignment_id`, `course_id`, `user_id`, `language`, `code`, `status`, `attempt_no`, `submitted_at`
) VALUES (
    @asm_id, @c_id, @s1_id, 'java', 'public class A {}', 'PENDING', 1, NOW()
);

-- ========================================================
-- HOÀN TẤT
-- ========================================================
SET FOREIGN_KEY_CHECKS=1;
COMMIT;