-- ========================================================
-- APSAS SAFE MEGA SEED (APPEND ONLY - NO DELETE)
-- Strategy: INSERT IGNORE + Dynamic Linking
-- ========================================================

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;

-- 1. ROLES (Đảm bảo có đủ role)
INSERT IGNORE INTO `roles` (`name`, `description`) VALUES
('ADMIN', 'Administrator'), ('LECTURER', 'Instructor'),
('STUDENT', 'Learner'), ('CONTENT_PROVIDER', 'Content Creator');

-- Lấy ID Role để dùng
SET @r_stu = (SELECT id FROM roles WHERE name='STUDENT' LIMIT 1);
SET @r_lec = (SELECT id FROM roles WHERE name='LECTURER' LIMIT 1);
SET @r_prov = (SELECT id FROM roles WHERE name='CONTENT_PROVIDER' LIMIT 1);

-- 2. USERS (Tạo thêm 50 sinh viên nếu chưa có)
-- Sử dụng CTE để sinh số từ 1 đến 50
INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`)
WITH RECURSIVE seq AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 50)
SELECT
    CONCAT('Student ', LPAD(n, 3, '0')),
    CONCAT('student', n, '@apsas.edu.vn'),
    '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE'
FROM seq;

-- Gán Role cho user vừa tạo (nếu chưa có role)
INSERT IGNORE INTO `users_roles` (`users_id`, `roles_id`)
SELECT id, @r_stu FROM users WHERE email LIKE 'student%@apsas.edu.vn';

-- Tạo Provider/Lecturer nếu thiếu
INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`) VALUES
('Main Provider', 'provider@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE'),
('Dr. Lecturer', 'lecturer@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');

-- Lấy ID Provider để tạo data
SET @provider_id = (SELECT id FROM users WHERE email='provider@apsas.edu.vn' LIMIT 1);
SET @lecturer_id = (SELECT id FROM users WHERE email='lecturer@apsas.edu.vn' LIMIT 1);

-- Gán role staff
INSERT IGNORE INTO `users_roles` (`users_id`, `roles_id`) VALUES
(@provider_id, @r_prov), (@lecturer_id, @r_lec);

-- 3. SKILLS (Thêm skill nếu thiếu)
INSERT IGNORE INTO `skills` (`name`, `category`, `created_by`) VALUES
('Java Arrays', 'ARRAY', @provider_id), ('Java String', 'STRING', @provider_id),
('Sorting Algo', 'SORTING', @provider_id), ('Graph Algo', 'GRAPH', @provider_id),
('Dynamic Prog', 'DYNAMIC_PROGRAMMING', @provider_id);

-- 4. TUTORIALS (10 Tutorials)
INSERT IGNORE INTO `tutorials` (`created_by`, `title`, `summary`, `status`) VALUES
(@provider_id, 'Java Zero to Hero', 'Basic', 'PUBLISHED'),
(@provider_id, 'Data Structures Ultimate', 'DSA', 'PUBLISHED'),
(@provider_id, 'Algorithms Advanced', 'Hard', 'PUBLISHED'),
(@provider_id, 'Spring Boot Guide', 'Web', 'PUBLISHED'),
(@provider_id, 'Database Design', 'SQL', 'PUBLISHED'),
(@provider_id, 'Microservices Pattern', 'Cloud', 'PUBLISHED'),
(@provider_id, 'Docker & K8s', 'DevOps', 'PUBLISHED'),
(@provider_id, 'React Frontend', 'JS', 'PUBLISHED'),
(@provider_id, 'Mobile Android', 'Kotlin', 'PUBLISHED'),
(@provider_id, 'System Design', 'Arch', 'PUBLISHED');

-- 5. COURSES (20 Courses)
-- Sinh mã khóa học CS201 -> CS220
INSERT IGNORE INTO `courses` (`name`, `code`, `description`, `visibility`, `type`, `created_by`)
WITH RECURSIVE seq AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 20)
SELECT
    CONCAT('Advanced CS ', 200 + n),
    CONCAT('CS', 200 + n),
    'Course description...',
    'PUBLIC',
    CASE WHEN n % 2 = 0 THEN 'CORE' ELSE 'ELECTIVE' END,
    @lecturer_id
FROM seq;

-- 6. CONTENTS (3 content per Tutorial)
INSERT IGNORE INTO `contents` (`tutorial_id`, `title`, `body_md`, `order_no`, `status`)
SELECT t.id, CONCAT('Intro to ', t.title), '# Chapter 1...', 1, 'PUBLISHED'
FROM tutorials t;

INSERT IGNORE INTO `contents` (`tutorial_id`, `title`, `body_md`, `order_no`, `status`)
SELECT t.id, CONCAT('Advanced ', t.title), '# Chapter 2...', 2, 'PUBLISHED'
FROM tutorials t;

-- 7. ASSIGNMENTS (Tự động tạo bài tập cho các Tutorial đang có)
-- Lấy skill ngẫu nhiên để gán
SET @s_array = (SELECT id FROM skills WHERE category='ARRAY' LIMIT 1);

INSERT IGNORE INTO `assignments` (`tutorial_id`, `skill_id`, `title`, `statement_md`, `max_score`, `proficiency`, `attempts_limit`)
SELECT
    t.id,
    (SELECT id FROM skills ORDER BY RAND() LIMIT 1), -- Random skill
    CONCAT('Practice for ', t.title),
    '# Problem Statement\nSolve this...',
    100.00,
    1,
    10
FROM tutorials t;

-- 8. ASSIGNMENT CONFIG (Test Cases)
-- Chỉ tạo config cho assignment nào CHƯA CÓ config
INSERT IGNORE INTO `assignment_evaluations` (`assignment_id`, `name`, `type`, `config_json`, `created_at`)
SELECT
    a.id, 'Auto Config', 'UNIT_TEST',
    '{ "testCase": [ {"in": "1", "out": "1", "visibility": "PUBLIC"}, {"in": "2", "out": "2", "visibility": "PRIVATE"} ] }',
    NOW()
FROM assignments a
WHERE NOT EXISTS (SELECT 1 FROM assignment_evaluations ae WHERE ae.assignment_id = a.id);

-- 9. LINKING: Course -> Assignment
-- Link ngẫu nhiên assignments vào courses (nếu chưa link)
INSERT IGNORE INTO `courses_assignments` (`courses_id`, `assignments_id`, `open_at`, `due_at`)
SELECT
    c.id, a.id, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)
FROM courses c
         JOIN assignments a ON (c.id + a.id) % 5 = 0; -- Logic random

-- 10. ENROLLMENTS (Ghi danh sinh viên vào khóa học)
-- Mỗi sinh viên đăng ký ~5 khóa học
INSERT IGNORE INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`)
SELECT
    u.id, c.id, 'STUDENT', NOW()
FROM users u
         CROSS JOIN courses c
WHERE u.email LIKE 'student%'
  AND (u.id + c.id) % 7 = 0; -- Logic random để không phải ai cũng học hết

-- 11. SUBMISSIONS (Tạo bài nộp)
-- Chỉ tạo submission cho những cặp (user, course, assignment) hợp lệ
-- JSON Report chuẩn DTO ReportCongfigSubmission
INSERT IGNORE INTO `submissions`
(`user_id`, `course_id`, `assignment_id`, `language`, `code`, `status`, `score`, `passed`, `attempt_no`, `submitted_at`, `report_json`)
SELECT
    e.user_id,
    e.course_id,
    ca.assignments_id,
    'java',
    'public class Solution { ... }',
    CASE WHEN RAND() > 0.4 THEN 'COMPLETE' ELSE 'FAILED' END,
    CASE WHEN RAND() > 0.4 THEN 100.00 ELSE 0.00 END,
    CASE WHEN RAND() > 0.4 THEN 1 ELSE 0 END,
    1, -- Attempt 1
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 10) DAY),
    -- JSON DYNAMIC
    CASE
        WHEN RAND() > 0.4 THEN
            '{
              "averageTime": 0.05, "averageMemory": 15000, "totalTestCases": 2, "passedTestCases": 2,
              "testCases": [
                {"status": "Accepted", "time": 0.01, "memory": 15000, "visibility": "PUBLIC", "stdin": "1", "stdout": "1", "expectedOutput": "1"},
                {"status": "Accepted", "time": 0.02, "memory": 15000, "visibility": "PRIVATE", "stdin": "2", "stdout": "2", "expectedOutput": "2"}
              ]
            }'
        ELSE
            '{
              "averageTime": 0.05, "averageMemory": 15000, "totalTestCases": 2, "passedTestCases": 0,
              "testCases": [
                {"status": "Wrong Answer", "time": 0.01, "memory": 15000, "visibility": "PUBLIC", "stdin": "1", "stdout": "0", "expectedOutput": "1"}
              ]
            }'
        END
FROM enrollments e
         JOIN courses_assignments ca ON e.course_id = ca.courses_id
WHERE RAND() < 0.5; -- 50% sinh viên làm bài

-- 12. FEEDBACK & NOTIFICATIONS (Bơm thêm dữ liệu)
INSERT INTO `feedback` (`submission_id`, `body`, `created_at`)
SELECT id, 'Good effort!', NOW() FROM submissions ORDER BY RAND() LIMIT 20;

INSERT INTO `notifications` (`user_id`, `type`, `payload`, `is_read`, `created_at`)
SELECT id, 'SYSTEM', '{"msg": "System Update"}', 0, NOW() FROM users WHERE email LIKE 'student%' LIMIT 30;

COMMIT;
SET FOREIGN_KEY_CHECKS=1;

-- CHECK COUNT
SELECT
    (SELECT COUNT(*) FROM users) as 'Total Users',
    (SELECT COUNT(*) FROM courses) as 'Total Courses',
    (SELECT COUNT(*) FROM assignments) as 'Total Assignments',
    (SELECT COUNT(*) FROM submissions) as 'Total Submissions';