-- phpMyAdmin SQL Dump - Đã được tối ưu hóa cho Docker Init
-- Tác giả: Chuyên gia Phần mềm
-- Ngày: 2025-10-22

-- TẮT KIỂM TRA KHÓA NGOẠI: Giúp import thành công các bảng có mối quan hệ phụ thuộc.
SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `apsas`
--

-- --------------------------------------------------------

--
-- Table structure for table `assignments`
--

CREATE TABLE `assignments` (
                               `id` char(36) NOT NULL DEFAULT (uuid()),
                               `tutorial_id` char(36) DEFAULT NULL,
                               `skill_id` char(36) DEFAULT NULL,
                               `title` varchar(200) NOT NULL,
                               `statement_md` mediumtext DEFAULT NULL,
                               `max_score` decimal(6,2) DEFAULT NULL,
                               `attempts_limit` int(10) UNSIGNED DEFAULT NULL,
                               `proficiency` varchar(80) DEFAULT NULL,
                               `created_at` datetime DEFAULT current_timestamp(),
                               `open_at` datetime DEFAULT NULL,
                               `due_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `assignment_evaluations`
--

CREATE TABLE `assignment_evaluations` (
                                          `id` char(36) NOT NULL DEFAULT (uuid()),
                                          `name` varchar(160) NOT NULL,
                                          `type` varchar(80) NOT NULL,
                                          `config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`config_json`)),
                                          `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `assignment_evaluation_maps`
--

CREATE TABLE `assignment_evaluation_maps` (
                                              `assignment_id` char(36) NOT NULL,
                                              `evaluation_id` char(36) NOT NULL,
                                              `weight` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `contents`
--

CREATE TABLE `contents` (
                            `id` char(36) NOT NULL DEFAULT (uuid()),
                            `tutorial_id` char(36) NOT NULL,
                            `title` varchar(200) NOT NULL,
                            `body_md` mediumtext DEFAULT NULL,
                            `body_html_cached` mediumtext DEFAULT NULL,
                            `order_no` int(11) DEFAULT NULL,
                            `status` enum('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT',

    `created_at` datetime DEFAULT current_timestamp()
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `courses`
--

CREATE TABLE `courses` (
                           `id` char(36) NOT NULL DEFAULT (uuid()),
                           `name` varchar(160) NOT NULL,
                           `code` varchar(60) DEFAULT NULL,
                           `visibility` enum('PUBLIC','PRIVATE','UNLISTED') DEFAULT 'PUBLIC',

    `limit` int(10) UNSIGNED DEFAULT NULL,
    `created_at` datetime DEFAULT current_timestamp()
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `course_assignments`
--

CREATE TABLE `course_assignments` (
                                      `course_id` char(36) NOT NULL,
                                      `assignment_id` char(36) NOT NULL,
                                      `open_at` datetime DEFAULT NULL,
                                      `due_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `course_contents`
--

CREATE TABLE `course_contents` (
                                   `course_id` char(36) NOT NULL,
                                   `content_id` char(36) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `enrollments`
--

CREATE TABLE `enrollments` (
                               `user_id` char(36) NOT NULL,
                               `course_id` char(36) NOT NULL,
                               `role` enum('OWNER','TEACHER','TA','STUDENT') DEFAULT 'STUDENT',
    `joined_at` datetime DEFAULT current_timestamp()
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `feedback`
--

CREATE TABLE `feedback` (
                            `id` char(36) NOT NULL DEFAULT (uuid()),
                            `body` text NOT NULL,
                            `created_at` datetime DEFAULT current_timestamp(),
                            `submission_id` char(36) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `help_requests`
--

CREATE TABLE `help_requests` (
                                 `id` char(36) NOT NULL DEFAULT (uuid()),
                                 `user_id` char(36) NOT NULL,
                                 `course_id` char(36) NOT NULL,
                                 `title` varchar(200) NOT NULL,
                                 `body` text DEFAULT NULL,
                                 `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `media`
--

CREATE TABLE `media` (
                         `id` char(36) NOT NULL DEFAULT (uuid()),
                         `content_id` char(36) NOT NULL,
                         `type` enum('IMAGE','VIDEO','AUDIO','FILE','LINK') NOT NULL,
    `url` varchar(1024) NOT NULL,
    `caption` varchar(255) DEFAULT NULL,
    `order_no` int(11) DEFAULT NULL,
    `created_at` datetime DEFAULT current_timestamp()
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
                                 `id` char(36) NOT NULL DEFAULT (uuid()),
                                 `user_id` char(36) NOT NULL,
                                 `type` varchar(64) NOT NULL,
                                 `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`payload`)),
                                 `is_read` tinyint(1) DEFAULT 0,
                                 `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `otps`
--

CREATE TABLE `otps` (
                        `id` char(36) NOT NULL DEFAULT (uuid()),
                        `user_id` char(36) NOT NULL,
                        `code` varchar(16) NOT NULL,
                        `expires_at` datetime NOT NULL,
                        `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `permissions`
--

CREATE TABLE `permissions` (
                               `id` char(36) NOT NULL DEFAULT (uuid()),
                               `name` varchar(120) NOT NULL,
                               `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `profiles`
--

CREATE TABLE `profiles` (
                            `id` char(36) NOT NULL DEFAULT (uuid()),
                            `user_id` char(36) NOT NULL,
                            `avatar_url` varchar(512) DEFAULT NULL,
                            `dob` date DEFAULT NULL,
                            `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
    `phone` varchar(30) DEFAULT NULL,
    `address` varchar(255) DEFAULT NULL,
    `bio` text DEFAULT NULL,
    `created_at` datetime NOT NULL DEFAULT current_timestamp()
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `progress`
--

CREATE TABLE `progress` (
                            `id` char(36) NOT NULL DEFAULT (uuid()),
                            `user_id` char(36) NOT NULL,
                            `total_attempt_no` int(11) DEFAULT NULL,
                            `acceptance` varchar(80) DEFAULT NULL,
                            `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `progress_skills`
--

CREATE TABLE `progress_skills` (
                                   `progress_id` char(36) NOT NULL,
                                   `skill_id` char(36) NOT NULL,
                                   `level` int(11) DEFAULT NULL,
                                   `score` decimal(6,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `refresh_tokens`
--

CREATE TABLE `refresh_tokens` (
                                  `id` char(36) NOT NULL DEFAULT (uuid()),
                                  `user_id` char(36) NOT NULL,
                                  `name` varchar(100) DEFAULT NULL,
                                  `token_hash` char(64) NOT NULL,
                                  `created_at` datetime DEFAULT current_timestamp(),
                                  `expires_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
                         `id` char(36) NOT NULL DEFAULT (uuid()),
                         `name` varchar(80) NOT NULL,
                         `description` varchar(255) DEFAULT NULL,
                         `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `role_permissions`
--

CREATE TABLE `role_permissions` (
                                    `role_id` char(36) NOT NULL,
                                    `permission_id` char(36) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `skills`
--

CREATE TABLE `skills` (
                          `id` char(36) NOT NULL DEFAULT (uuid()),
                          `name` varchar(160) NOT NULL,
                          `description` text DEFAULT NULL,
                          `category` varchar(120) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `submissions`
--

CREATE TABLE `submissions` (
                               `id` char(36) NOT NULL DEFAULT (uuid()),
                               `assignment_id` char(36) NOT NULL,
                               `user_id` char(36) NOT NULL,
                               `language` varchar(40) DEFAULT NULL,
                               `code` mediumtext DEFAULT NULL,
                               `report_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`report_json`)),
                               `score` decimal(6,2) DEFAULT NULL,
                               `feedback` text DEFAULT NULL,
                               `passed` tinyint(1) DEFAULT NULL,
                               `attempt_no` int(10) UNSIGNED DEFAULT 1,
                               `submitted_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `tutorials`
--

CREATE TABLE `tutorials` (
                             `id` char(36) NOT NULL DEFAULT (uuid()),
                             `created_by` char(36) DEFAULT NULL,
                             `title` varchar(200) NOT NULL,
                             `summary` text DEFAULT NULL,
                             `status` enum('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT',
    `created_at` datetime DEFAULT current_timestamp()
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
                         `id` char(36) NOT NULL DEFAULT (uuid()),
                         `name` varchar(120) NOT NULL,
                         `email` varchar(190) NOT NULL,
                         `password` varchar(255) NOT NULL,
                         `status` enum('ACTIVE','INACTIVE','BANNED') DEFAULT 'ACTIVE',
    `created_at` datetime NOT NULL DEFAULT current_timestamp()
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_notifications`
--

CREATE TABLE `user_notifications` (
                                      `user_id` char(36) NOT NULL,
                                      `notification_id` char(36) NOT NULL,
                                      `is_read` tinyint(1) DEFAULT 0,
                                      `read_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_roles`
--

CREATE TABLE `user_roles` (
                              `user_id` char(36) NOT NULL,
                              `role_id` char(36) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `assignments`
--
ALTER TABLE `assignments`
    ADD PRIMARY KEY (`id`),
  ADD KEY `ix_assignments_tutorial` (`tutorial_id`),
  ADD KEY `ix_assignments_skill` (`skill_id`);

--
-- Indexes for table `assignment_evaluations`
--
ALTER TABLE `assignment_evaluations`
    ADD PRIMARY KEY (`id`);

--
-- Indexes for table `assignment_evaluation_maps`
--
ALTER TABLE `assignment_evaluation_maps`
    ADD PRIMARY KEY (`assignment_id`,`evaluation_id`),
  ADD KEY `ix_aem_eval` (`evaluation_id`);

--
-- Indexes for table `contents`
--
ALTER TABLE `contents`
    ADD PRIMARY KEY (`id`),
  ADD KEY `ix_contents_tutorial` (`tutorial_id`);

--
-- Indexes for table `courses`
--
ALTER TABLE `courses`
    ADD PRIMARY KEY (`id`);

--
-- Indexes for table `course_assignments`
--
ALTER TABLE `course_assignments`
    ADD PRIMARY KEY (`course_id`,`assignment_id`),
  ADD KEY `ix_ca_assignment` (`assignment_id`,`open_at`,`due_at`);

--
-- Indexes for table `course_contents`
--
ALTER TABLE `course_contents`
    ADD PRIMARY KEY (`course_id`,`content_id`),
  ADD KEY `ix_cc_content` (`content_id`);

--
-- Indexes for table `enrollments`
--
ALTER TABLE `enrollments`
    ADD PRIMARY KEY (`user_id`,`course_id`),
  ADD KEY `ix_enrollments_course` (`course_id`,`role`);

--
-- Indexes for table `feedback`
--
ALTER TABLE `feedback`
    ADD PRIMARY KEY (`id`),
  ADD KEY `ix_feedback_submission` (`submission_id`,`created_at`);

--
-- Indexes for table `help_requests`
--
ALTER TABLE `help_requests`
    ADD PRIMARY KEY (`id`),
  ADD KEY `ix_help_requests_user` (`user_id`,`created_at`),
  ADD KEY `ix_help_requests_course` (`course_id`,`created_at`);

--
-- Indexes for table `media`
--
ALTER TABLE `media`
    ADD PRIMARY KEY (`id`),
  ADD KEY `ix_media_content` (`content_id`,`order_no`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
    ADD PRIMARY KEY (`id`);

--
-- Indexes for table `otps`
--
ALTER TABLE `otps`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_otps_user` (`user_id`);

--
-- Indexes for table `permissions`
--
ALTER TABLE `permissions`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `profiles`
--
ALTER TABLE `profiles`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_profiles_user` (`user_id`);

--
-- Indexes for table `progress`
--
ALTER TABLE `progress`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_progress_user` (`user_id`);

--
-- Indexes for table `progress_skills`
--
ALTER TABLE `progress_skills`
    ADD PRIMARY KEY (`progress_id`,`skill_id`),
  ADD KEY `ix_progress_skills_skill` (`skill_id`);

--
-- Indexes for table `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token_hash` (`token_hash`),
  ADD UNIQUE KEY `uq_refresh_tokens_user` (`user_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `role_permissions`
--
ALTER TABLE `role_permissions`
    ADD PRIMARY KEY (`role_id`,`permission_id`),
  ADD KEY `permission_id` (`permission_id`);

--
-- Indexes for table `skills`
--
ALTER TABLE `skills`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `submissions`
--
ALTER TABLE `submissions`
    ADD PRIMARY KEY (`id`),
  ADD KEY `fk_submissions_user` (`user_id`),
  ADD KEY `ix_submissions_assignment` (`assignment_id`,`submitted_at`);

--
-- Indexes for table `tutorials`
--
ALTER TABLE `tutorials`
    ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `user_notifications`
--
ALTER TABLE `user_notifications`
    ADD PRIMARY KEY (`user_id`,`notification_id`),
  ADD KEY `ix_user_notifications_user` (`user_id`),
  ADD KEY `ix_user_notifications_notif` (`notification_id`);

--
-- Indexes for table `user_roles`
--
ALTER TABLE `user_roles`
    ADD PRIMARY KEY (`user_id`,`role_id`),
  ADD KEY `role_id` (`role_id`);
--
-- Constraints for table `help_requests`
--
ALTER TABLE `help_requests`
    ADD CONSTRAINT `fk_help_requests_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_help_requests_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `assignments`
--
ALTER TABLE `assignments`
    ADD CONSTRAINT `assignments_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  ADD CONSTRAINT `fk_assignments_tutorial` FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `assignment_evaluation_maps`
--
ALTER TABLE `assignment_evaluation_maps`
    ADD CONSTRAINT `assignment_evaluation_maps_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `assignment_evaluation_maps_ibfk_2` FOREIGN KEY (`evaluation_id`) REFERENCES `assignment_evaluations` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `contents`
--
ALTER TABLE `contents`
    ADD CONSTRAINT `fk_contents_tutorial` FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `course_assignments`
--
ALTER TABLE `course_assignments`
    ADD CONSTRAINT `course_assignments_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `course_assignments_ibfk_2` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `course_contents`
--
ALTER TABLE `course_contents`
    ADD CONSTRAINT `course_contents_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `course_contents_ibfk_2` FOREIGN KEY (`content_id`) REFERENCES `contents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `enrollments`
--
ALTER TABLE `enrollments`
    ADD CONSTRAINT `fk_enrollments_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_enrollments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `feedback`
--
ALTER TABLE `feedback`
    ADD CONSTRAINT `fk_feedback_submission` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `media`
--
ALTER TABLE `media`
    ADD CONSTRAINT `fk_media_content` FOREIGN KEY (`content_id`) REFERENCES `contents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `otps`
--
ALTER TABLE `otps`
    ADD CONSTRAINT `fk_otps_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `profiles`
--
ALTER TABLE `profiles`
    ADD CONSTRAINT `fk_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `progress`
--
ALTER TABLE `progress`
    ADD CONSTRAINT `fk_progress_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `progress_skills`
--
ALTER TABLE `progress_skills`
    ADD CONSTRAINT `progress_skills_ibfk_1` FOREIGN KEY (`progress_id`) REFERENCES `progress` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `progress_skills_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
    ADD CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `role_permissions`
--
ALTER TABLE `role_permissions`
    ADD CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `submissions`
--
ALTER TABLE `submissions`
    ADD CONSTRAINT `fk_submissions_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_submissions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user_notifications`
--
ALTER TABLE `user_notifications`
    ADD CONSTRAINT `user_notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_notifications_ibfk_2` FOREIGN KEY (`notification_id`) REFERENCES `notifications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user_roles`
--
ALTER TABLE `user_roles`
    ADD CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
SET FOREIGN_KEY_CHECKS=1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;