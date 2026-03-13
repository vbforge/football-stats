-- Step 1.2: Add season_id to existing tables

-- Add season_id to games table
ALTER TABLE `games` ADD COLUMN `season_id` bigint NOT NULL DEFAULT 1;
ALTER TABLE `games` ADD CONSTRAINT `FK_games_season`
    FOREIGN KEY (`season_id`) REFERENCES `seasons` (`id`);

-- Add season_id to match_days table
ALTER TABLE `match_days` ADD COLUMN `season_id` bigint NOT NULL DEFAULT 1;
ALTER TABLE `match_days` ADD CONSTRAINT `FK_match_days_season`
    FOREIGN KEY (`season_id`) REFERENCES `seasons` (`id`);

-- Update unique constraint on match_days to include season
ALTER TABLE `match_days` DROP INDEX `UKpyvghf90b5jxs08t4kfk9hus4`;
ALTER TABLE `match_days` ADD UNIQUE KEY `UK_match_day_season` (`number`, `season_id`);