-- Step 1.1: Create seasons table
CREATE TABLE `seasons` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(100) NOT NULL,
                           `start_date` date NOT NULL,
                           `end_date` date NOT NULL,
                           `is_current` boolean DEFAULT false,
                           `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `UK_season_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert current season 2025-26
INSERT INTO seasons (name, start_date, end_date, is_current)
VALUES ('2025-26', '2025-08-15', '2026-05-24', true);