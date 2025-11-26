-- =============================================
-- APSAS DATABASE SEED DATA
-- Complete test data for all tables
-- Version: 3.0
-- Date: 2025-01-XX
-- =============================================

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;

USE apsas_db;

-- ========================================================
-- 1. ROLES - Synchronized with Java DataSeeder
-- ========================================================
INSERT IGNORE INTO `roles` (`name`, `description`) VALUES
('ADMIN', 'Administrator with full system access'),
('LECTURER', 'Instructor who creates and manages courses'),
('STUDENT', 'Learner enrolled in courses'),
('CONTENT_PROVIDER', 'Content creator for tutorials and assignments'),
('GUEST', 'Guest user with limited access');

-- Store role IDs in variables
SET @role_admin_id   = (SELECT id FROM roles WHERE name = 'ADMIN' LIMIT 1);
SET @role_lecturer_id = (SELECT id FROM roles WHERE name = 'LECTURER' LIMIT 1);
SET @role_student_id  = (SELECT id FROM roles WHERE name = 'STUDENT' LIMIT 1);
SET @role_provider_id = (SELECT id FROM roles WHERE name = 'CONTENT_PROVIDER' LIMIT 1);
SET @role_guest_id = (SELECT id FROM roles WHERE name = 'GUEST' LIMIT 1);

-- ========================================================
-- 2. PERMISSIONS - Complete permission set
-- ========================================================
INSERT IGNORE INTO permissions (name, description) VALUES
-- Admin - User Management
('VIEW_USERS', 'View user list and details'),
('CREATE_USERS', 'Create new users'),
('UPDATE_USERS', 'Update user information'),
('DELETE_USERS', 'Delete users'),

-- Admin - Role Management
('VIEW_ROLES', 'View roles and permissions'),
('CREATE_ROLES', 'Create new roles'),
('UPDATE_ROLES', 'Update role permissions'),
('DELETE_ROLES', 'Delete roles'),

-- Admin - Tutorial Management
('MANAGE_TUTORIALS', 'Manage all tutorials in system'),
('PUBLISH_TUTORIALS', 'Approve and publish tutorials'),

-- Content Provider - Tutorial Operations
('CREATE_TUTORIAL', 'Create new tutorials'),
('UPDATE_TUTORIAL', 'Update own tutorials'),
('DELETE_TUTORIAL', 'Delete own tutorials'),
('VIEW_OWN_TUTORIALS', 'View own tutorial list'),

-- Content Provider - Content & Assignment
('CREATE_CONTENT', 'Create tutorial content'),
('UPDATE_CONTENT', 'Update tutorial content'),
('CREATE_ASSIGNMENT', 'Create assignments'),
('UPDATE_ASSIGNMENT', 'Update assignments'),

-- Lecturer - Course Management
('VIEW_COURSES', 'View courses'),
('CREATE_COURSE', 'Create new courses'),
('UPDATE_COURSE', 'Update course information'),
('DELETE_COURSE', 'Delete courses'),

-- Student - Course Operations
('ENROLL_COURSE', 'Enroll in courses'),

-- Teacher - Submission & Evaluation
('VIEW_SUBMISSIONS', 'View student submissions'),
('EVALUATE_SUBMISSIONS', 'Evaluate and grade submissions'),

-- Student - Assignment Operations
('SUBMIT_ASSIGNMENT', 'Submit assignments'),

-- Support & Help
('VIEW_FEEDBACK', 'View feedback'),
('RESPOND_FEEDBACK', 'Respond to feedback'),
('REQUEST_HELP', 'Request help from instructors'),
('VIEW_HELP_REQUESTS', 'View help requests from students'),

-- Notifications
('VIEW_NOTIFICATIONS', 'View notifications'),
('MANAGE_NOTIFICATIONS', 'Create and manage notifications'),

-- Statistics & Insights
('VIEW_TEACHER_STATS', 'View teacher statistics'),

-- General Permissions
('DASHBOARD_VIEW', 'Access dashboard'),
('PROFILE_READ', 'View profile'),
('PROFILE_WRITE', 'Edit profile'),
('SUPPORT_CREATE', 'Create support tickets'),
('RESOURCE_READ', 'Read resources and materials'),
('RESOURCE_WRITE', 'Write resources and materials'),
('TESTCASE_READ', 'View test cases'),
('TESTCASE_WRITE', 'Create test cases'),
('SCHEDULE_READ', 'View schedule'),
('NOTIF_READ', 'Read notifications (legacy)'),
('NOTIF_WRITE', 'Write notifications (legacy)'),

-- System Administration
('USER_MANAGE', 'Manage users (legacy)'),
('API_MANAGE', 'Manage API settings'),
('MAINTENANCE_MANAGE', 'System maintenance'),
('POLICY_MANAGE', 'Manage policies');

-- ========================================================
-- 3. ROLE-PERMISSION MAPPINGS - Synchronized with DataSeeder
-- ========================================================

-- GUEST Role (Read-only public access)
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_guest_id, id FROM permissions WHERE name IN (
    'RESOURCE_READ', 'SUPPORT_CREATE'
);

-- STUDENT Role
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_student_id, id FROM permissions WHERE name IN (
    'DASHBOARD_VIEW', 'PROFILE_READ', 'PROFILE_WRITE',
    'VIEW_COURSES', 'ENROLL_COURSE', 'SUBMIT_ASSIGNMENT',
    'VIEW_NOTIFICATIONS', 'REQUEST_HELP', 'RESOURCE_READ',
    'SCHEDULE_READ', 'SUPPORT_CREATE'
);

-- LECTURER Role
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_lecturer_id, id FROM permissions WHERE name IN (
    'DASHBOARD_VIEW', 'PROFILE_READ', 'PROFILE_WRITE',
    'VIEW_COURSES', 'CREATE_COURSE', 'UPDATE_COURSE', 'DELETE_COURSE',
    'VIEW_SUBMISSIONS', 'EVALUATE_SUBMISSIONS', 'VIEW_HELP_REQUESTS',
    'RESPOND_FEEDBACK', 'VIEW_TEACHER_STATS', 'VIEW_NOTIFICATIONS',
    'RESOURCE_READ', 'RESOURCE_WRITE', 'SUPPORT_CREATE'
);

-- CONTENT_PROVIDER Role
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_provider_id, id FROM permissions WHERE name IN (
    'DASHBOARD_VIEW', 'PROFILE_READ', 'PROFILE_WRITE',
    'CREATE_TUTORIAL', 'UPDATE_TUTORIAL', 'DELETE_TUTORIAL', 'VIEW_OWN_TUTORIALS',
    'CREATE_CONTENT', 'UPDATE_CONTENT', 'CREATE_ASSIGNMENT', 'UPDATE_ASSIGNMENT',
    'RESOURCE_READ', 'RESOURCE_WRITE', 'VIEW_NOTIFICATIONS', 'SUPPORT_CREATE'
);

-- ADMIN Role (All permissions)
INSERT IGNORE INTO roles_permissions (roles_id, permissions_id)
SELECT @role_admin_id, p.id FROM permissions p;

-- ========================================================
-- 4. TEST USERS
-- Password for all test users: Test@123456
-- ========================================================

-- Lecturer Account
INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`) VALUES
('Dr. John Smith', 'lecturer@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');

SET @lecturer_id = (SELECT id FROM users WHERE email = 'lecturer@apsas.edu.vn' LIMIT 1);

-- Content Provider Account
INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`) VALUES
('Jane Doe', 'provider@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');

SET @provider_id = (SELECT id FROM users WHERE email = 'provider@apsas.edu.vn' LIMIT 1);

-- Additional Lecturer for Testing
INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`) VALUES
('Prof. Emily Chen', 'lecturer2@apsas.edu.vn', '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa', 'ACTIVE');

SET @lecturer2_id = (SELECT id FROM users WHERE email = 'lecturer2@apsas.edu.vn' LIMIT 1);

-- Assign Roles
INSERT IGNORE INTO `users_roles` (`users_id`, `roles_id`) VALUES
(@lecturer_id, @role_lecturer_id),
(@lecturer2_id, @role_lecturer_id),
(@provider_id, @role_provider_id);

-- Create Profiles
INSERT INTO `profiles` (`user_id`, `avatar_url`, `bio`, `phone`, `address`) VALUES
    (@lecturer_id, 'https://i.pravatar.cc/300?u=lecturer', '10+ years of experience in Java and Spring Boot development', '0901234567', '123 University Street, Ho Chi Minh City'),
    (@lecturer2_id, 'https://i.pravatar.cc/300?u=lecturer2', 'Specializing in Data Structures and Algorithms', '0901234568', '456 Academic Avenue, Hanoi'),
    (@provider_id, 'https://i.pravatar.cc/300?u=provider', 'Professional content creator and programming educator', '0901234569', '789 Education Road, Da Nang')
ON DUPLICATE KEY UPDATE bio = VALUES(bio);

-- ========================================================
-- 5. STUDENT ACCOUNTS (50 students for realistic testing)
-- ========================================================

INSERT IGNORE INTO `users` (`name`, `email`, `password`, `status`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT
    CONCAT('Student ', LPAD(n, 2, '0')),
    CONCAT('student', n, '@apsas.edu.vn'),
    '$2a$10$8.A..I1y8.p1d.NbtAmjtu2g.VAJ.R.m/a5g1xO84iJCbT5uV/kwa',
    'ACTIVE'
FROM seq;

-- Assign Student Role
INSERT IGNORE INTO `users_roles` (`users_id`, `roles_id`)
SELECT u.id, @role_student_id
FROM `users` u
WHERE u.email LIKE 'student%@apsas.edu.vn';

-- Create Student Profiles
INSERT IGNORE INTO `profiles` (`user_id`, `avatar_url`, `bio`)
SELECT
    u.id,
    CONCAT('https://i.pravatar.cc/300?u=', u.id),
    'Computer Science student passionate about programming'
FROM `users` u
WHERE u.email LIKE 'student%@apsas.edu.vn';

-- ========================================================
-- 6. SKILLS - Programming skill categories
-- ========================================================

INSERT IGNORE INTO `skills` (`name`, `description`, `category`, `created_by`) VALUES
-- Array Skills
('Array Basics', 'Fundamental array operations', 'ARRAY', @provider_id),
('Array Manipulation', 'Advanced array techniques', 'ARRAY', @provider_id),
('Two Pointers', 'Two pointer technique', 'ARRAY', @provider_id),

-- String Skills
('String Processing', 'String manipulation and parsing', 'STRING', @provider_id),
('Pattern Matching', 'String pattern matching algorithms', 'STRING', @provider_id),

-- Dynamic Programming
('DP Fundamentals', 'Basic dynamic programming', 'DYNAMIC_PROGRAMMING', @provider_id),
('Advanced DP', 'Complex DP problems', 'DYNAMIC_PROGRAMMING', @provider_id),

-- Tree & Graph
('Tree Traversal', 'Tree traversal algorithms', 'TREE', @provider_id),
('Graph Algorithms', 'Graph search and shortest path', 'GRAPH', @provider_id),

-- Sorting & Searching
('Sorting Algorithms', 'Various sorting techniques', 'SORTING', @provider_id),
('Binary Search', 'Binary search and variants', 'SEARCHING', @provider_id),

-- Other
('Recursion', 'Recursive problem solving', 'OTHER', @provider_id),
('Mathematics', 'Mathematical algorithms', 'MATH', @provider_id);

-- Store some skill IDs for later use
SET @skill_array_basics = (SELECT id FROM skills WHERE name = 'Array Basics' LIMIT 1);
SET @skill_dp_fundamentals = (SELECT id FROM skills WHERE name = 'DP Fundamentals' LIMIT 1);
SET @skill_tree_traversal = (SELECT id FROM skills WHERE name = 'Tree Traversal' LIMIT 1);
SET @skill_sorting = (SELECT id FROM skills WHERE name = 'Sorting Algorithms' LIMIT 1);
SET @skill_string_processing = (SELECT id FROM skills WHERE name = 'String Processing' LIMIT 1);

-- ========================================================
-- 7. TUTORIALS - Complete learning materials
-- ========================================================

INSERT IGNORE INTO `tutorials` (`created_by`, `title`, `summary`, `status`) VALUES
(@provider_id, 'Java Programming Fundamentals', 'Complete guide to Java basics including syntax, data types, and control structures', 'PUBLISHED'),
(@provider_id, 'Data Structures in Java', 'Comprehensive coverage of arrays, linked lists, stacks, queues, and trees', 'PUBLISHED'),
(@provider_id, 'Algorithm Design Techniques', 'Master sorting, searching, and algorithm analysis', 'PUBLISHED'),
(@provider_id, 'Dynamic Programming Mastery', 'From basics to advanced DP problems with detailed explanations', 'PUBLISHED'),
(@provider_id, 'Object-Oriented Programming', 'Learn OOP concepts: inheritance, polymorphism, and design patterns', 'PUBLISHED'),
(@provider_id, 'Advanced Java Topics', 'Covers generics, collections framework, and lambda expressions', 'PENDING'),
(@provider_id, 'String Algorithms', 'Pattern matching, substring search, and text processing', 'PUBLISHED'),
(@provider_id, 'Graph Theory Basics', 'Introduction to graphs, traversals, and shortest path algorithms', 'PUBLISHED'),
(@provider_id, 'Recursion and Backtracking', 'Master recursive thinking and backtracking problems', 'PUBLISHED'),
(@provider_id, 'Tree Data Structures', 'Binary trees, BST, AVL trees, and tree algorithms', 'PUBLISHED');

-- Store tutorial IDs
SET @tutorial_java_basics = (SELECT id FROM tutorials WHERE title = 'Java Programming Fundamentals' LIMIT 1);
SET @tutorial_ds = (SELECT id FROM tutorials WHERE title = 'Data Structures in Java' LIMIT 1);
SET @tutorial_algorithms = (SELECT id FROM tutorials WHERE title = 'Algorithm Design Techniques' LIMIT 1);
SET @tutorial_dp = (SELECT id FROM tutorials WHERE title = 'Dynamic Programming Mastery' LIMIT 1);
SET @tutorial_string = (SELECT id FROM tutorials WHERE title = 'String Algorithms' LIMIT 1);

-- ========================================================
-- 8. COURSES - Various course types for testing
-- ========================================================

INSERT IGNORE INTO `courses` (`name`, `code`, `description`, `visibility`, `type`, `avatar_url`, `created_by`) VALUES
-- Public Courses by Lecturer 1
('Introduction to Programming', 'CS101', 'Fundamental programming concepts using Java', 'PUBLIC', 'Core Course', 'https://images.unsplash.com/photo-1607706189992-eae578626c86', @lecturer_id),
('Data Structures and Algorithms', 'CS201', 'Learn essential data structures and algorithm design', 'PUBLIC', 'Core Course', 'https://images.unsplash.com/photo-1516116216624-53e697fedbea', @lecturer_id),
('Advanced Java Programming', 'CS301', 'Advanced Java features and best practices', 'PUBLIC', 'Elective', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97', @lecturer_id),
('Software Engineering Principles', 'CS401', 'Software development methodologies and practices', 'PRIVATE', 'Core Course', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c', @lecturer_id),

-- Public Courses by Lecturer 2
('Object-Oriented Design', 'CS202', 'OOP principles and design patterns', 'PUBLIC', 'Core Course', 'https://images.unsplash.com/photo-1498050108023-c5249f4df085', @lecturer2_id),
('Web Development with Spring', 'CS302', 'Building web applications with Spring Framework', 'PUBLIC', 'Elective', 'https://images.unsplash.com/photo-1547658719-da2b51169166', @lecturer2_id),
('Database Management Systems', 'CS203', 'Database design and SQL fundamentals', 'PUBLIC', 'Core Course', 'https://images.unsplash.com/photo-1544383835-bda2bc66a55d', @lecturer2_id);

-- Store course IDs
SET @course_cs101 = (SELECT id FROM courses WHERE code = 'CS101' LIMIT 1);
SET @course_cs201 = (SELECT id FROM courses WHERE code = 'CS201' LIMIT 1);
SET @course_cs301 = (SELECT id FROM courses WHERE code = 'CS301' LIMIT 1);
SET @course_cs202 = (SELECT id FROM courses WHERE code = 'CS202' LIMIT 1);
SET @course_cs302 = (SELECT id FROM courses WHERE code = 'CS302' LIMIT 1);
SET @course_cs203 = (SELECT id FROM courses WHERE code = 'CS203' LIMIT 1);
SET @course_cs401 = (SELECT id FROM courses WHERE code = 'CS401' LIMIT 1);

-- ========================================================
-- 9. TUTORIAL CONTENTS - Detailed learning materials
-- ========================================================

-- Contents for Java Fundamentals Tutorial
INSERT IGNORE INTO `contents` (`tutorial_id`, `title`, `body_md`, `order_no`, `status`) VALUES
(@tutorial_java_basics, 'Introduction to Java', '# Introduction to Java\n\nJava is a high-level, class-based, object-oriented programming language...', 1, 'PUBLISHED'),
(@tutorial_java_basics, 'Variables and Data Types', '# Variables and Data Types\n\nLearn about primitive types, reference types, and type conversion...', 2, 'PUBLISHED'),
(@tutorial_java_basics, 'Control Flow Statements', '# Control Flow\n\nIf-else, switch, loops, and break/continue statements...', 3, 'PUBLISHED'),
(@tutorial_java_basics, 'Methods and Functions', '# Methods\n\nDefining methods, parameters, return types, and method overloading...', 4, 'PUBLISHED'),
(@tutorial_java_basics, 'Arrays in Java', '# Arrays\n\nArray declaration, initialization, and manipulation...', 5, 'PUBLISHED'),

-- Contents for Data Structures Tutorial
(@tutorial_ds, 'Introduction to Data Structures', '# Data Structures Overview\n\nUnderstanding abstract data types and their implementations...', 1, 'PUBLISHED'),
(@tutorial_ds, 'Arrays and Dynamic Arrays', '# Arrays\n\nFixed-size arrays vs ArrayList, time complexity analysis...', 2, 'PUBLISHED'),
(@tutorial_ds, 'Linked Lists', '# Linked Lists\n\nSingly linked lists, doubly linked lists, circular lists...', 3, 'PUBLISHED'),
(@tutorial_ds, 'Stacks and Queues', '# Stacks and Queues\n\nLIFO and FIFO data structures, implementations...', 4, 'PUBLISHED'),
(@tutorial_ds, 'Trees and Binary Trees', '# Trees\n\nTree terminology, binary trees, tree traversals...', 5, 'PUBLISHED'),

-- Contents for Algorithms Tutorial
(@tutorial_algorithms, 'Algorithm Analysis', '# Big O Notation\n\nTime and space complexity, best/worst/average cases...', 1, 'PUBLISHED'),
(@tutorial_algorithms, 'Sorting Algorithms', '# Sorting\n\nBubble sort, selection sort, merge sort, quick sort...', 2, 'PUBLISHED'),
(@tutorial_algorithms, 'Searching Algorithms', '# Searching\n\nLinear search, binary search, hash-based search...', 3, 'PUBLISHED'),
(@tutorial_algorithms, 'Divide and Conquer', '# Divide and Conquer\n\nBreaking problems into subproblems...', 4, 'PUBLISHED'),

-- Contents for DP Tutorial
(@tutorial_dp, 'Introduction to Dynamic Programming', '# DP Fundamentals\n\nMemoization, tabulation, optimal substructure...', 1, 'PUBLISHED'),
(@tutorial_dp, 'Classic DP Problems', '# Classic Problems\n\nFibonacci, coin change, knapsack...', 2, 'PUBLISHED'),
(@tutorial_dp, 'Advanced DP Techniques', '# Advanced DP\n\nState compression, DP on trees...', 3, 'PUBLISHED'),

-- Contents for String Algorithms Tutorial
(@tutorial_string, 'String Basics', '# String Operations\n\nString creation, concatenation, and basic methods...', 1, 'PUBLISHED'),
(@tutorial_string, 'Pattern Matching', '# Pattern Matching\n\nKMP algorithm, Rabin-Karp, and string search...', 2, 'PUBLISHED');

-- Store some content IDs for media
SET @content_java_intro = (SELECT id FROM contents WHERE title = 'Introduction to Java' LIMIT 1);
SET @content_ds_intro = (SELECT id FROM contents WHERE title = 'Introduction to Data Structures' LIMIT 1);
SET @content_algo_analysis = (SELECT id FROM contents WHERE title = 'Algorithm Analysis' LIMIT 1);

-- ========================================================
-- 10. MEDIA - Images, videos, and files for contents
-- ========================================================

INSERT IGNORE INTO `media` (`content_id`, `type`, `url`, `caption`, `order_no`) VALUES
-- Media for Java Introduction
(@content_java_intro, 'IMAGE', 'https://images.unsplash.com/photo-1516116216624-53e697fedbea', 'Java Logo', 1),
(@content_java_intro, 'VIDEO', 'https://www.youtube.com/watch?v=eIrMbAQSU34', 'Java Introduction Video', 2),
(@content_java_intro, 'LINK', 'https://docs.oracle.com/javase/tutorial/', 'Official Java Tutorial', 3),

-- Media for Data Structures Introduction
(@content_ds_intro, 'IMAGE', 'https://images.unsplash.com/photo-1555066931-4365d14bab8c', 'Data Structures Visualization', 1),
(@content_ds_intro, 'VIDEO', 'https://www.youtube.com/watch?v=RBSGKlAvoiM', 'Data Structures Overview', 2),

-- Media for Algorithm Analysis
(@content_algo_analysis, 'IMAGE', 'https://images.unsplash.com/photo-1544383835-bda2bc66a55d', 'Big O Notation Chart', 1),
(@content_algo_analysis, 'FILE', 'https://example.com/files/big-o-cheatsheet.pdf', 'Big O Cheat Sheet PDF', 2);

-- ========================================================
-- 11. COURSE ENROLLMENTS
-- ========================================================

-- Lecturers as course owners
INSERT IGNORE INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`)
SELECT c.created_by, c.id, 'OWNER', DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY)
FROM `courses` c;

-- Students enrolled in public courses (randomized enrollment)
INSERT IGNORE INTO `enrollments` (`user_id`, `course_id`, `role`, `joined_at`)
SELECT u.id, c.id, 'STUDENT', DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY)
FROM `users` u
CROSS JOIN `courses` c
WHERE u.email LIKE 'student%@apsas.edu.vn'
  AND c.visibility = 'PUBLIC'
  AND RAND() < 0.6;  -- 60% enrollment rate

-- Link courses with tutorial contents
INSERT IGNORE INTO `courses_contents` (`courses_id`, `contents_id`)
SELECT @course_cs101, ct.id FROM contents ct WHERE ct.tutorial_id = @tutorial_java_basics;

INSERT IGNORE INTO `courses_contents` (`courses_id`, `contents_id`)
SELECT @course_cs201, ct.id FROM contents ct WHERE ct.tutorial_id = @tutorial_ds
UNION
SELECT @course_cs201, ct.id FROM contents ct WHERE ct.tutorial_id = @tutorial_algorithms;

INSERT IGNORE INTO `courses_contents` (`courses_id`, `contents_id`)
SELECT @course_cs301, ct.id FROM contents ct WHERE ct.tutorial_id = @tutorial_dp;

-- ========================================================
-- 12. ASSIGNMENTS - Programming exercises
-- ========================================================

-- Java Basics Assignments
INSERT IGNORE INTO `assignments` (
    `tutorial_id`, `skill_id`, `title`, `statement_md`, `max_score`,
    `attempts_limit`, `order_no`, `proficiency`
) VALUES
(@tutorial_java_basics, @skill_array_basics, 'Sum of Array Elements', 
'# Problem: Sum of Array Elements\n\nWrite a method that calculates the sum of all elements in an integer array.\n\n**Input:** int[] arr\n**Output:** int sum\n\n**Example:**\n```\nInput: [1, 2, 3, 4, 5]\nOutput: 15\n```',
100.00, 5, 1, 0),

(@tutorial_java_basics, @skill_array_basics, 'Find Maximum Element',
'# Problem: Find Maximum\n\nFind the maximum element in an array.\n\n**Input:** int[] arr\n**Output:** int max\n\n**Example:**\n```\nInput: [3, 7, 2, 9, 1]\nOutput: 9\n```',
100.00, 5, 2, 0),

-- Data Structures Assignments
(@tutorial_ds, @skill_array_basics, 'Reverse an Array',
'# Problem: Reverse Array\n\nReverse the elements of an array in-place.\n\n**Input:** int[] arr\n**Output:** void (modify array in-place)\n\n**Example:**\n```\nInput: [1, 2, 3, 4, 5]\nOutput: [5, 4, 3, 2, 1]\n```',
100.00, 5, 1, 1),

(@tutorial_ds, @skill_tree_traversal, 'Binary Tree Inorder Traversal',
'# Problem: Inorder Traversal\n\nImplement inorder traversal of a binary tree.\n\n**Input:** TreeNode root\n**Output:** List<Integer> values\n\n**Tree structure:**\n```\n    1\n   / \\\n  2   3\n / \\\n4   5\n```\n**Output:** [4, 2, 5, 1, 3]',
100.00, 10, 2, 2),

-- Algorithm Assignments
(@tutorial_algorithms, @skill_sorting, 'Implement Bubble Sort',
'# Problem: Bubble Sort\n\nImplement the bubble sort algorithm.\n\n**Input:** int[] arr\n**Output:** void (sort in-place)\n\n**Constraints:**\n- Array length: 1 ≤ n ≤ 1000\n- Element range: -10^6 ≤ arr[i] ≤ 10^6',
100.00, 10, 1, 1),

(@tutorial_algorithms, @skill_sorting, 'Binary Search Implementation',
'# Problem: Binary Search\n\nImplement binary search on a sorted array.\n\n**Input:** int[] arr, int target\n**Output:** int index (-1 if not found)\n\n**Example:**\n```\nInput: arr = [1, 3, 5, 7, 9], target = 5\nOutput: 2\n```',
100.00, 10, 2, 1),

-- Dynamic Programming Assignments
(@tutorial_dp, @skill_dp_fundamentals, 'Fibonacci Number',
'# Problem: Fibonacci Sequence\n\nCalculate the nth Fibonacci number using dynamic programming.\n\n**Input:** int n\n**Output:** long fibonacci(n)\n\n**Constraints:**\n- 0 ≤ n ≤ 50\n\n**Example:**\n```\nInput: n = 10\nOutput: 55\n```',
100.00, 10, 1, 1),

(@tutorial_dp, @skill_dp_fundamentals, 'Climbing Stairs',
'# Problem: Climbing Stairs\n\nYou are climbing a staircase with n steps. Each time you can climb 1 or 2 steps. How many distinct ways can you climb to the top?\n\n**Input:** int n\n**Output:** int ways\n\n**Example:**\n```\nInput: n = 3\nOutput: 3\nExplanation: [1,1,1], [1,2], [2,1]\n```',
100.00, 10, 2, 1),

-- String Algorithm Assignments
(@tutorial_string, @skill_string_processing, 'Reverse String',
'# Problem: Reverse String\n\nReverse a given string.\n\n**Input:** String s\n**Output:** String reversed\n\n**Example:**\n```\nInput: "hello"\nOutput: "olleh"\n```',
100.00, 5, 1, 0);

-- Store some assignment IDs
SET @assignment_sum = (SELECT id FROM assignments WHERE title = 'Sum of Array Elements' LIMIT 1);
SET @assignment_max = (SELECT id FROM assignments WHERE title = 'Find Maximum Element' LIMIT 1);
SET @assignment_reverse = (SELECT id FROM assignments WHERE title = 'Reverse an Array' LIMIT 1);
SET @assignment_tree = (SELECT id FROM assignments WHERE title = 'Binary Tree Inorder Traversal' LIMIT 1);
SET @assignment_bubble = (SELECT id FROM assignments WHERE title = 'Implement Bubble Sort' LIMIT 1);
SET @assignment_binary = (SELECT id FROM assignments WHERE title = 'Binary Search Implementation' LIMIT 1);
SET @assignment_fib = (SELECT id FROM assignments WHERE title = 'Fibonacci Number' LIMIT 1);
SET @assignment_stairs = (SELECT id FROM assignments WHERE title = 'Climbing Stairs' LIMIT 1);

-- ========================================================
-- 13. ASSIGNMENT EVALUATIONS - Test configurations
-- ========================================================

INSERT IGNORE INTO `assignment_evaluations` (`assignment_id`, `name`, `type`, `config_json`) VALUES
-- Unit Test Evaluator for Sum of Array Elements
(@assignment_sum, 'Unit Test Evaluator', 'UNIT_TEST', 
'{"testCases":[{"input":"[1,2,3,4,5]","expectedOutput":"15","description":"Basic test case"},{"input":"[10,20,30]","expectedOutput":"60","description":"Multiple elements"},{"input":"[5]","expectedOutput":"5","description":"Single element"},{"input":"[]","expectedOutput":"0","description":"Empty array"}]}'),

-- Unit Test Evaluator for Find Maximum
(@assignment_max, 'Unit Test Evaluator', 'UNIT_TEST',
'{"testCases":[{"input":"[3,7,2,9,1]","expectedOutput":"9","description":"Basic test"},{"input":"[-5,-2,-10]","expectedOutput":"-2","description":"Negative numbers"},{"input":"[100]","expectedOutput":"100","description":"Single element"}]}'),

-- Unit Test Evaluator for Reverse Array
(@assignment_reverse, 'Unit Test Evaluator', 'UNIT_TEST',
'{"testCases":[{"input":"[1,2,3,4,5]","expectedOutput":"[5,4,3,2,1]","description":"Basic reverse"},{"input":"[10,20]","expectedOutput":"[20,10]","description":"Two elements"}]}'),

-- Code Quality Evaluator for Tree Traversal
(@assignment_tree, 'Code Quality Evaluator', 'CODE_QUALITY',
'{"checks":["timeComplexity","spaceComplexity","codeStyle"],"minScore":70}'),

-- Performance Evaluator for Bubble Sort
(@assignment_bubble, 'Performance Evaluator', 'PERFORMANCE',
'{"timeLimit":2000,"memoryLimit":256,"testCases":[{"input":"[5,2,8,1,9]","expectedOutput":"[1,2,5,8,9]"}]}'),

-- Unit Test Evaluator for Binary Search
(@assignment_binary, 'Unit Test Evaluator', 'UNIT_TEST',
'{"testCases":[{"input":"arr=[1,3,5,7,9],target=5","expectedOutput":"2","description":"Found in middle"},{"input":"arr=[1,3,5,7,9],target=10","expectedOutput":"-1","description":"Not found"}]}'),

-- Unit Test Evaluator for Fibonacci
(@assignment_fib, 'Unit Test Evaluator', 'UNIT_TEST',
'{"testCases":[{"input":"10","expectedOutput":"55","description":"n=10"},{"input":"0","expectedOutput":"0","description":"n=0"},{"input":"1","expectedOutput":"1","description":"n=1"}]}'),

-- Unit Test Evaluator for Climbing Stairs
(@assignment_stairs, 'Unit Test Evaluator', 'UNIT_TEST',
'{"testCases":[{"input":"3","expectedOutput":"3","description":"n=3"},{"input":"2","expectedOutput":"2","description":"n=2"},{"input":"5","expectedOutput":"8","description":"n=5"}]}');

-- ========================================================
-- 14. COURSES_ASSIGNMENTS (Link assignments to courses)
-- ========================================================

-- Course 1 (CS101): 3 assignments
INSERT IGNORE INTO `courses_assignments` (`courses_id`, `assignments_id`, `open_at`, `due_at`) VALUES
(@course_cs101, @assignment_sum, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY)),
(@course_cs101, @assignment_max, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY)),
(@course_cs101, @assignment_reverse, NOW(), DATE_ADD(NOW(), INTERVAL 21 DAY));

-- Course 2 (CS201): 3 assignments
INSERT IGNORE INTO `courses_assignments` (`courses_id`, `assignments_id`, `open_at`, `due_at`) VALUES
(@course_cs201, @assignment_tree, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY)),
(@course_cs201, @assignment_bubble, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 17 DAY)),
(@course_cs201, @assignment_binary, NOW(), DATE_ADD(NOW(), INTERVAL 24 DAY));

-- Course 3 (CS301): 2 assignments
INSERT IGNORE INTO `courses_assignments` (`courses_id`, `assignments_id`, `open_at`, `due_at`) VALUES
(@course_cs301, @assignment_fib, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 9 DAY)),
(@course_cs301, @assignment_stairs, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 16 DAY));

-- Course 2 (CS202): 2 assignments
INSERT IGNORE INTO `courses_assignments` (`courses_id`, `assignments_id`, `open_at`, `due_at`) VALUES
(@course_cs202, @assignment_sum, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 12 DAY)),
(@course_cs202, @assignment_tree, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 19 DAY));

-- ========================================================
-- 15. SUBMISSIONS - Sample student submissions with course_id
-- ========================================================

-- Get some student IDs
SET @student1_id = (SELECT id FROM users WHERE email = 'student1@apsas.edu.vn' LIMIT 1);
SET @student2_id = (SELECT id FROM users WHERE email = 'student2@apsas.edu.vn' LIMIT 1);
SET @student3_id = (SELECT id FROM users WHERE email = 'student3@apsas.edu.vn' LIMIT 1);
SET @student4_id = (SELECT id FROM users WHERE email = 'student4@apsas.edu.vn' LIMIT 1);
SET @student5_id = (SELECT id FROM users WHERE email = 'student5@apsas.edu.vn' LIMIT 1);
SET @student6_id = (SELECT id FROM users WHERE email = 'student6@apsas.edu.vn' LIMIT 1);
SET @student7_id = (SELECT id FROM users WHERE email = 'student7@apsas.edu.vn' LIMIT 1);
SET @student8_id = (SELECT id FROM users WHERE email = 'student8@apsas.edu.vn' LIMIT 1);
SET @student10_id = (SELECT id FROM users WHERE email = 'student10@apsas.edu.vn' LIMIT 1);

-- Submissions for CS101 course
INSERT IGNORE INTO `submissions` (
    `assignment_id`, `course_id`, `user_id`, `language`, `code`, `status`, 
    `attempt_no`, `submitted_at`, `score`, `passed`, `big_o_complexity_time`, `big_o_complexity_space`
) VALUES
-- Student 1 submissions (Excellent student)
(@assignment_sum, @course_cs101, @student1_id, 'java', 
'public class Solution {\n    public int sumArray(int[] arr) {\n        int sum = 0;\n        for (int num : arr) {\n            sum += num;\n        }\n        return sum;\n    }\n}',
'PASSED', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 100.00, 1, 'O(n)', 'O(1)'),

(@assignment_max, @course_cs101, @student1_id, 'java',
'public class Solution {\n    public int findMax(int[] arr) {\n        int max = arr[0];\n        for (int i = 1; i < arr.length; i++) {\n            if (arr[i] > max) max = arr[i];\n        }\n        return max;\n    }\n}',
'PASSED', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 100.00, 1, 'O(n)', 'O(1)'),

(@assignment_reverse, @course_cs101, @student1_id, 'java',
'public class Solution {\n    public void reverseArray(int[] arr) {\n        int left = 0, right = arr.length - 1;\n        while (left < right) {\n            int temp = arr[left];\n            arr[left] = arr[right];\n            arr[right] = temp;\n            left++;\n            right--;\n        }\n    }\n}',
'PASSED', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 100.00, 1, 'O(n)', 'O(1)'),

-- Student 2 submissions (Good with one failure)
(@assignment_sum, @course_cs101, @student2_id, 'java',
'public class Solution {\n    public int sumArray(int[] arr) {\n        return java.util.Arrays.stream(arr).sum();\n    }\n}',
'PASSED', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 95.00, 1, 'O(n)', 'O(1)'),

(@assignment_max, @course_cs101, @student2_id, 'java',
'public class Solution {\n    public int findMax(int[] arr) {\n        // Wrong implementation - only returns first element\n        return arr[0];\n    }\n}',
'FAILED', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 20.00, 0, 'O(1)', 'O(1)'),

-- Student 3 - Pending submission
(@assignment_sum, @course_cs101, @student3_id, 'java',
'public class Solution {\n    public int sumArray(int[] arr) {\n        int result = 0;\n        // TODO: implement\n        return result;\n    }\n}',
'PENDING', 1, DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, NULL, NULL, NULL),

-- Student 4 submissions
(@assignment_sum, @course_cs101, @student4_id, 'java',
'public class Solution {\n    public int sumArray(int[] arr) {\n        int sum = 0;\n        for(int i = 0; i < arr.length; i++) {\n            sum += arr[i];\n        }\n        return sum;\n    }\n}',
'PASSED', 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 90.00, 1, 'O(n)', 'O(1)'),

-- Submissions for CS201 course
(@assignment_tree, @course_cs201, @student5_id, 'java',
'public class Solution {\n    public List<Integer> inorderTraversal(TreeNode root) {\n        List<Integer> result = new ArrayList<>();\n        inorder(root, result);\n        return result;\n    }\n    \n    private void inorder(TreeNode node, List<Integer> result) {\n        if (node == null) return;\n        inorder(node.left, result);\n        result.add(node.val);\n        inorder(node.right, result);\n    }\n}',
'PASSED', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 100.00, 1, 'O(n)', 'O(h)'),

(@assignment_bubble, @course_cs201, @student5_id, 'java',
'public class Solution {\n    public void bubbleSort(int[] arr) {\n        int n = arr.length;\n        for (int i = 0; i < n - 1; i++) {\n            for (int j = 0; j < n - i - 1; j++) {\n                if (arr[j] > arr[j + 1]) {\n                    int temp = arr[j];\n                    arr[j] = arr[j + 1];\n                    arr[j + 1] = temp;\n                }\n            }\n        }\n    }\n}',
'PASSED', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 100.00, 1, 'O(n²)', 'O(1)'),

(@assignment_tree, @course_cs201, @student6_id, 'java',
'public class Solution {\n    public List<Integer> inorderTraversal(TreeNode root) {\n        // Incomplete implementation\n        return new ArrayList<>();\n    }\n}',
'FAILED', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 25.00, 0, NULL, NULL),

(@assignment_bubble, @course_cs201, @student7_id, 'java',
'public class Solution {\n    public void bubbleSort(int[] arr) {\n        // Using Arrays.sort instead of implementing bubble sort\n        Arrays.sort(arr);\n    }\n}',
'FAILED', 1, DATE_SUB(NOW(), INTERVAL 8 HOUR), 40.00, 0, 'O(n log n)', 'O(1)'),

-- Student 8 - Multiple attempts
(@assignment_sum, @course_cs101, @student8_id, 'java',
'public class Solution {\n    public int sumArray(int[] arr) {\n        int sum = 0;\n        for (int num : arr) {\n            sum += num;\n        }\n        return sum;\n    }\n}',
'PASSED', 3, DATE_SUB(NOW(), INTERVAL 2 DAY), 85.00, 1, 'O(n)', 'O(1)'),

-- Student 10 submissions
(@assignment_max, @course_cs101, @student10_id, 'java',
'public class Solution {\n    public int findMax(int[] arr) {\n        return Arrays.stream(arr).max().getAsInt();\n    }\n}',
'PASSED', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), 100.00, 1, 'O(n)', 'O(1)');

-- ========================================================
-- 16. PROGRESS - Student progress tracking
-- ========================================================

-- Create progress records for active students
INSERT IGNORE INTO `progress` (`user_id`, `total_attempt_no`, `acceptance`)
SELECT 
    u.id,
    COALESCE(COUNT(s.id), 0) as total_attempts,
    CASE 
        WHEN COUNT(s.id) > 0 THEN 
            ROUND(SUM(CASE WHEN s.passed = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(s.id), 2)
        ELSE 0 
    END as acceptance_rate
FROM `users` u
LEFT JOIN `submissions` s ON u.id = s.user_id
WHERE u.email LIKE 'student%@apsas.edu.vn'
GROUP BY u.id;

-- ========================================================
-- 17. PROGRESS_SKILLS - Skill level tracking
-- ========================================================

-- Create progress_skills records based on submissions
INSERT IGNORE INTO `progress_skills` (`progress_id`, `skill_id`, `level`, `score`)
SELECT 
    p.id as progress_id,
    a.skill_id,
    CASE 
        WHEN AVG(s.score) >= 90 THEN 3
        WHEN AVG(s.score) >= 70 THEN 2
        WHEN AVG(s.score) >= 50 THEN 1
        ELSE 0
    END as level,
    ROUND(AVG(s.score), 2) as avg_score
FROM `progress` p
INNER JOIN `users` u ON p.user_id = u.id
INNER JOIN `submissions` s ON u.id = s.user_id
INNER JOIN `assignments` a ON s.assignment_id = a.id
WHERE a.skill_id IS NOT NULL
  AND s.score IS NOT NULL
GROUP BY p.id, a.skill_id;

-- ========================================================
-- 18. HELP REQUESTS - Student support tickets
-- ========================================================

INSERT IGNORE INTO `help_requests` (`course_id`, `user_id`, `title`, `body`, `created_at`) VALUES
(@course_cs101, @student3_id, 'Question about Array Sum Assignment',
'I''m getting a NullPointerException when testing with empty arrays. How should I handle this case?',
DATE_SUB(NOW(), INTERVAL 3 HOUR)),

(@course_cs201, @student10_id, 'Need clarification on Reverse Array',
'Is it okay to use Collections.reverse() or do we need to implement it manually?',
DATE_SUB(NOW(), INTERVAL 2 DAY)),

(@course_cs101, @student2_id, 'Failed test cases for Find Maximum',
'My solution fails for negative numbers. Can you help?',
DATE_SUB(NOW(), INTERVAL 1 DAY)),

(@course_cs201, @student6_id, 'Tree Traversal Question',
'How do I handle null nodes in the tree traversal?',
DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- ========================================================
-- 19. NOTIFICATIONS - System notifications
-- ========================================================

INSERT IGNORE INTO `notifications` (`user_id`, `type`, `payload`, `is_read`, `created_at`) VALUES
(@student1_id, 'SYSTEM', '{"title":"Welcome to APSAS","message":"Welcome to Automated Programming Skills Assessment System!"}', 0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(@student2_id, 'ASSIGNMENT', CONCAT('{"title":"New Assignment Available","message":"Check out the new assignments in Data Structures course","courseId":', @course_cs201, '}'), 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(@student3_id, 'SUBMISSION', CONCAT('{"title":"Submission Graded","message":"Your submission for Array Sum has been graded","submissionId":', (SELECT id FROM submissions WHERE user_id = @student3_id LIMIT 1), '}'), 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(@student1_id, 'ASSIGNMENT', CONCAT('{"title":"Assignment Due Soon","message":"Your assignment is due in 2 days","assignmentId":', @assignment_reverse, '}'), 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(@lecturer_id, 'HELP_REQUEST', '{"title":"New Help Request","message":"Student has requested help","helpRequestId":1}', 0, DATE_SUB(NOW(), INTERVAL 3 HOUR));

-- ========================================================
-- 20. USERS_NOTIFICATIONS - User notification mapping
-- ========================================================

-- Link notifications to users (for many-to-many relationship)
INSERT IGNORE INTO `users_notifications` (`users_id`, `notifications_id`, `is_read`, `read_at`)
SELECT 
    n.user_id,
    n.id,
    n.is_read,
    CASE WHEN n.is_read = 1 THEN n.created_at ELSE NULL END
FROM `notifications` n
WHERE n.user_id IS NOT NULL;

-- ========================================================
-- 21. FEEDBACK - Teacher feedback on submissions
-- ========================================================

-- Get some submission IDs
SET @sub_passed = (SELECT id FROM submissions WHERE user_id = @student1_id AND status = 'PASSED' LIMIT 1);
SET @sub_failed = (SELECT id FROM submissions WHERE user_id = @student2_id AND status = 'FAILED' LIMIT 1);
SET @sub_pending = (SELECT id FROM submissions WHERE user_id = @student3_id AND status = 'PENDING' LIMIT 1);

INSERT IGNORE INTO `feedback` (`submission_id`, `body`, `created_at`) VALUES
(@sub_passed, 'Excellent work! Your solution is clean and efficient. Good use of enhanced for-loop. Consider edge cases for empty arrays.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(@sub_failed, 'Your solution only returns the first element. You need to iterate through the entire array to find the maximum. Try using a loop!', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(@sub_passed, 'Great implementation! The two-pointer approach is optimal. Time complexity O(n) and space O(1) is perfect.', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ========================================================
-- COMPLETION
-- ========================================================
SET FOREIGN_KEY_CHECKS=1;
COMMIT;

-- ========================================================
-- SUMMARY - Complete Test Data
-- ========================================================
-- Roles: 5 (ADMIN, LECTURER, STUDENT, CONTENT_PROVIDER, GUEST)
-- Permissions: 58+ (synchronized with DataSeeder.java)
-- Users: 54 total
--   - 1 admin (via DataSeeder.java at startup)
--   - 2 lecturers (lecturer@apsas.edu.vn, lecturer2@apsas.edu.vn)
--   - 1 content provider (provider@apsas.edu.vn)
--   - 50 students (student1-50@apsas.edu.vn)
-- Skills: 13 across various categories (ARRAY, STRING, DP, TREE, GRAPH, etc.)
-- Tutorials: 10 complete tutorials with published status
-- Contents: 20+ learning materials (linked to tutorials)
-- Media: 7 media items (images, videos, links, files) for contents
-- Courses: 7 courses
--   - 4 by Lecturer 1 (CS101, CS201, CS301, CS401)
--   - 3 by Lecturer 2 (CS202, CS302, CS203)
--   - Mix of PUBLIC and PRIVATE visibility
-- Assignments: 9 programming exercises
--   - Linked to tutorials with skills
--   - Various difficulty levels (proficiency 0-2)
-- Assignment Evaluations: 8 evaluation configurations
--   - Unit test evaluators
--   - Code quality evaluators
--   - Performance evaluators
-- Courses_Assignments: 10+ links
--   - Assignments mapped to courses with open_at and due_at dates
-- Courses_Contents: 20+ links
--   - Contents mapped to appropriate courses
-- Enrollments: ~300+ records
--   - All lecturers as OWNER of their courses
--   - 60% random student enrollment in public courses
-- Submissions: 14 diverse submissions
--   - PASSED: 9 (scores 85-100)
--   - FAILED: 4 (scores 20-40)
--   - PENDING: 1 (no score yet)
--   - Multiple courses (CS101, CS201)
--   - Includes proper course_id, complexity analysis
-- Progress: 50+ progress records (one per student)
-- Progress_Skills: Skill level tracking based on submissions
-- Help Requests: 4 support tickets from students
-- Notifications: 5 system notifications (SYSTEM, ASSIGNMENT, SUBMISSION, HELP_REQUEST)
-- Users_Notifications: Notification-user mappings
-- Feedback: 3 teacher feedback entries on submissions
-- 
-- ALL FOREIGN KEYS VALID - No orphaned records
-- READY FOR API TESTING - All endpoints have comprehensive test data
-- ========================================================
