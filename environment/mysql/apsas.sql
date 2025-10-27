-- phpMyAdmin SQL Dump - Đã được tối ưu hóa cho Docker Init
-- Tác giả: Chuyên gia Phần mềm
-- Ngày: 2025-10-22

SET FOREIGN_KEY_CHECKS=0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
 /*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
 /*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 /*!40101 SET NAMES utf8mb4 */;

-- Database: `apsas`

-- ========================================================
-- Base tables
-- ========================================================

CREATE TABLE `assignments` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `tutorial_id` BIGINT DEFAULT NULL,
                               `skill_id` BIGINT DEFAULT NULL,
                               `title` varchar(200) NOT NULL,
                               `statement_md` mediumtext DEFAULT NULL,
                               `max_score` decimal(6,2) DEFAULT NULL,
                               `order_no` int(11) DEFAULT NULL,
                               `attempts_limit` int(10) UNSIGNED DEFAULT NULL,
                               `proficiency` varchar(80) DEFAULT NULL,
                               `created_at` datetime DEFAULT current_timestamp(),
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `assignment_evaluations` (
                                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                                          `name` varchar(160) NOT NULL,
                                          `type` varchar(80) NOT NULL,

                                          `config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`config_json`)),
                                          `created_at` datetime DEFAULT current_timestamp(),



                                          PRIMARY KEY (`id`),

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `assignment_evaluation_maps` (
                                              `assignment_id` BIGINT NOT NULL,
                                              `evaluation_id` BIGINT NOT NULL,
                                              `weight` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `contents` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `tutorial_id` BIGINT NOT NULL,
                            `title` varchar(200) NOT NULL,
                            `body_md` mediumtext DEFAULT NULL,
                            `body_html_cached` mediumtext DEFAULT NULL,
                            `order_no` int(11) DEFAULT NULL,
                            `status` enum('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT',
                            `created_at` datetime DEFAULT current_timestamp(),
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `courses` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                           `name` varchar(160) NOT NULL,
                           `code` varchar(60) DEFAULT NULL,
                           `visibility` enum('PUBLIC','PRIVATE','UNLISTED') DEFAULT 'PUBLIC',
                           `limit` int(10) UNSIGNED DEFAULT NULL,
                           `created_at` datetime DEFAULT current_timestamp(),
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE `courses`
    ADD COLUMN `type` VARCHAR(50) DEFAULT '' AFTER `visibility`,
  ADD COLUMN `avatar_url` VARCHAR(255) DEFAULT NULL AFTER `type`;

CREATE TABLE `enrollments` (
                               `user_id` BIGINT NOT NULL,
                               `course_id` BIGINT NOT NULL,
                               `role` enum('OWNER','TEACHER','TA','STUDENT') DEFAULT 'STUDENT',
                               `joined_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `feedback` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `body` text NOT NULL,
                            `created_at` datetime DEFAULT current_timestamp(),
                            `submission_id` BIGINT NOT NULL,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `help_requests` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `user_id` BIGINT NOT NULL,
                                 `course_id` BIGINT NOT NULL,
                                 `title` varchar(200) NOT NULL,
                                 `body` text DEFAULT NULL,
                                 `created_at` datetime DEFAULT current_timestamp(),
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `media` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                         `content_id` BIGINT NOT NULL,
                         `type` enum('IMAGE','VIDEO','AUDIO','FILE','LINK') NOT NULL,
                         `url` varchar(1024) NOT NULL,
                         `caption` varchar(255) DEFAULT NULL,
                         `order_no` int(11) DEFAULT NULL,
                         `created_at` datetime DEFAULT current_timestamp(),
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `notifications` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `user_id` BIGINT NOT NULL,
                                 `type` varchar(64) NOT NULL,
                                 `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`payload`)),
                                 `is_read` tinyint(1) DEFAULT 0,
                                 `created_at` datetime DEFAULT current_timestamp(),
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `otps` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL,
                        `code` varchar(16) NOT NULL,
                        `expires_at` datetime NOT NULL,
                        `created_at` datetime DEFAULT current_timestamp(),
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `permissions` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `name` varchar(120) NOT NULL,
                               `description` text DEFAULT NULL,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `profiles` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `user_id` BIGINT NOT NULL,
                            `avatar_url` varchar(512) DEFAULT NULL,
                            `dob` date DEFAULT NULL,
                            `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
                            `phone` varchar(30) DEFAULT NULL,
                            `address` varchar(255) DEFAULT NULL,
                            `bio` text DEFAULT NULL,
                            `created_at` datetime NOT NULL DEFAULT current_timestamp(),
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `progress` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `user_id` BIGINT NOT NULL,
                            `total_attempt_no` int(11) DEFAULT NULL,
                            `acceptance` float DEFAULT NULL,
                            `created_at` datetime DEFAULT current_timestamp(),
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `progress_skills` (
                                   `progress_id` BIGINT NOT NULL,
                                   `skill_id` BIGINT NOT NULL,
                                   `level` int(11) DEFAULT NULL,
                                   `score` decimal(6,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `refresh_tokens` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `user_id` BIGINT NOT NULL,
                                  `name` varchar(100) DEFAULT NULL,
                                  `token_hash` varchar(255) NOT NULL,
                                  `created_at` datetime DEFAULT current_timestamp(),
                                  `expires_at` datetime DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `token_hash` (`token_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `roles` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                         `name` varchar(80) NOT NULL,
                         `description` text DEFAULT NULL,
                         `created_at` datetime DEFAULT current_timestamp(),
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `skills` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `name` varchar(160) NOT NULL,
                          `description` text DEFAULT NULL,
                          `category` ENUM(
  'ARRAY',
  'STRING',
  'LIST',
  'STACK',
  'QUEUE',
  'LINKED_LIST',
  'TREE',
  'GRAPH',
  'HASH_MAP',
  'HEAP',
  'SEARCHING',
  'SORTING',
  'GREEDY',
  'DIVIDE_AND_CONQUER',
  'DYNAMIC_PROGRAMMING',
  'BACKTRACKING',
  'GRAPH_ALGORITHM',
  'MATH',
  'BIT_MANIPULATION',
  'OTHER'
) DEFAULT 'OTHER',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

CREATE TABLE `submissions` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `assignment_id` BIGINT NOT NULL,
                               `user_id` BIGINT NOT NULL,
                               `language` varchar(40) DEFAULT NULL,
                               `code` mediumtext DEFAULT NULL,
                               `report_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`report_json`)),
                               `score` decimal(6,2) DEFAULT NULL,
                               `feedback` text DEFAULT NULL,
                               `passed` tinyint(1) DEFAULT NULL,
                               `attempt_no` int(10) UNSIGNED DEFAULT 1,
                               `submitted_at` datetime DEFAULT current_timestamp(),
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

CREATE TABLE `tutorials` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT,
                             `created_by` BIGINT DEFAULT NULL,
                             `title` varchar(200) NOT NULL,
                             `summary` text DEFAULT NULL,
                             `status` enum('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'DRAFT',
                             `created_at` datetime DEFAULT current_timestamp(),
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                         `name` varchar(120) NOT NULL,
                         `email` varchar(190) NOT NULL,
                         `password` varchar(255) NOT NULL,
                         `status` enum('ACTIVE','INACTIVE','BANNED') DEFAULT 'ACTIVE',
                         `created_at` datetime NOT NULL DEFAULT current_timestamp(),
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

-- ========================================================
-- Join tables (đã đổi tên cột FK sang plural_id)
-- ========================================================

CREATE TABLE `courses_assignments` (
                                       `courses_id` BIGINT NOT NULL,
                                       `assignments_id` BIGINT NOT NULL,
                                       `open_at` datetime DEFAULT NULL,
                                       `due_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `courses_contents` (
                                    `courses_id` BIGINT NOT NULL,
                                    `contents_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

CREATE TABLE `roles_permissions` (
                                     `roles_id` BIGINT NOT NULL,
                                     `permissions_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

CREATE TABLE `users_notifications` (
                                       `users_id` BIGINT NOT NULL,
                                       `notifications_id` BIGINT NOT NULL,
                                       `is_read` tinyint(1) DEFAULT 0,
                                       `read_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

CREATE TABLE `users_roles` (
                               `users_id` BIGINT NOT NULL,
                               `roles_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_GENERAL_ci;

-- ========================================================
-- Indexes
-- ========================================================

ALTER TABLE `assignments`
    ADD KEY `ix_assignments_tutorial` (`tutorial_id`),
  ADD KEY `ix_assignments_skill` (`skill_id`);

ALTER TABLE `assignment_evaluations` -- none
;

ALTER TABLE `assignment_evaluation_maps`
    ADD PRIMARY KEY (`assignment_id`,`evaluation_id`),
  ADD KEY `ix_aem_eval` (`evaluation_id`);

ALTER TABLE `contents`
    ADD KEY `ix_contents_tutorial` (`tutorial_id`);

ALTER TABLE `courses` -- none
;

ALTER TABLE `enrollments`
    ADD PRIMARY KEY (`user_id`,`course_id`),
  ADD KEY `ix_enrollments_course` (`course_id`,`role`);

ALTER TABLE `feedback`
    ADD KEY `ix_feedback_submission` (`submission_id`,`created_at`);

ALTER TABLE `help_requests`
    ADD KEY `ix_help_requests_user` (`user_id`,`created_at`),
  ADD KEY `ix_help_requests_course` (`course_id`,`created_at`);

ALTER TABLE `media`
    ADD KEY `ix_media_content` (`content_id`,`order_no`);

ALTER TABLE `notifications` -- none
;

ALTER TABLE `otps`
    ADD UNIQUE KEY `uq_otps_user` (`user_id`);

ALTER TABLE `profiles`
    ADD UNIQUE KEY `uq_profiles_user` (`user_id`);

ALTER TABLE `progress`
    ADD UNIQUE KEY `uq_progress_user` (`user_id`);

ALTER TABLE `progress_skills`
    ADD PRIMARY KEY (`progress_id`,`skill_id`),
  ADD KEY `ix_progress_skills_skill` (`skill_id`);

ALTER TABLE `refresh_tokens`
    ADD UNIQUE KEY `uq_refresh_tokens_user` (`user_id`);

ALTER TABLE `roles` -- none
;

ALTER TABLE `skills` -- none
;

ALTER TABLE `submissions`
    ADD KEY `fk_submissions_user` (`user_id`),
  ADD KEY `ix_submissions_assignment` (`assignment_id`,`submitted_at`);

ALTER TABLE `tutorials` -- none
;
ALTER TABLE `users` -- none
;

-- Join tables indexes (theo cột plural_id)
ALTER TABLE `courses_assignments`
    ADD PRIMARY KEY (`courses_id`,`assignments_id`),
  ADD KEY `ix_ca_assignment` (`assignments_id`,`open_at`,`due_at`);

ALTER TABLE `courses_contents`
    ADD PRIMARY KEY (`courses_id`,`contents_id`),
  ADD KEY `ix_cc_content` (`contents_id`);

ALTER TABLE `roles_permissions`
    ADD PRIMARY KEY (`roles_id`,`permissions_id`),
  ADD KEY `permissions_id` (`permissions_id`);

ALTER TABLE `users_notifications`
    ADD PRIMARY KEY (`users_id`,`notifications_id`),
  ADD KEY `ix_user_notifications_user` (`users_id`),
  ADD KEY `ix_user_notifications_notif` (`notifications_id`);

ALTER TABLE `users_roles`
    ADD PRIMARY KEY (`users_id`,`roles_id`),
  ADD KEY `roles_id` (`roles_id`);

-- ========================================================
-- Constraints
-- ========================================================

ALTER TABLE `help_requests`
    ADD CONSTRAINT `fk_help_requests_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_help_requests_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `assignments`
    ADD CONSTRAINT `assignments_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  ADD CONSTRAINT `fk_assignments_tutorial` FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `assignment_evaluation_maps`
    ADD CONSTRAINT `assignment_evaluation_maps_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `assignment_evaluation_maps_ibfk_2` FOREIGN KEY (`evaluation_id`) REFERENCES `assignment_evaluations` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `contents`
    ADD CONSTRAINT `fk_contents_tutorial` FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Join tables FKs (map theo cột plural_id)
ALTER TABLE `courses_assignments`
    ADD CONSTRAINT `courses_assignments_fk_course` FOREIGN KEY (`courses_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `courses_assignments_fk_assignment` FOREIGN KEY (`assignments_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `courses_contents`
    ADD CONSTRAINT `courses_contents_fk_course` FOREIGN KEY (`courses_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `courses_contents_fk_content` FOREIGN KEY (`contents_id`) REFERENCES `contents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `enrollments`
    ADD CONSTRAINT `fk_enrollments_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_enrollments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `feedback`
    ADD CONSTRAINT `fk_feedback_submission` FOREIGN KEY (`submission_id`) REFERENCES `submissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `media`
    ADD CONSTRAINT `fk_media_content` FOREIGN KEY (`content_id`) REFERENCES `contents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `otps`
    ADD CONSTRAINT `fk_otps_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `profiles`
    ADD CONSTRAINT `fk_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `progress`
    ADD CONSTRAINT `fk_progress_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `progress_skills`
    ADD CONSTRAINT `progress_skills_ibfk_1` FOREIGN KEY (`progress_id`) REFERENCES `progress` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `progress_skills_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `refresh_tokens`
    ADD CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;


ALTER TABLE `tutorials`
    ADD CONSTRAINT `fk_tutorials_created_by`
        FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
            ON DELETE SET NULL
            ON UPDATE CASCADE;


ALTER TABLE `roles_permissions`
    ADD CONSTRAINT `roles_permissions_ibfk_1` FOREIGN KEY (`roles_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `roles_permissions_ibfk_2` FOREIGN KEY (`permissions_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `submissions`
    ADD CONSTRAINT `fk_submissions_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_submissions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `users_notifications`
    ADD CONSTRAINT `users_notifications_ibfk_1` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `users_notifications_ibfk_2` FOREIGN KEY (`notifications_id`) REFERENCES `notifications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `users_roles`
    ADD CONSTRAINT `users_roles_ibfk_1` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `users_roles_ibfk_2` FOREIGN KEY (`roles_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

SET FOREIGN_KEY_CHECKS=1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
