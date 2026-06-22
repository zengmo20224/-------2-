-- Phase 8b: Progressive ban — extend phone_blacklist for history + duration
-- Run against dev MySQL. NOT idempotent (ALTER ADD COLUMN fails on re-run).

-- 1. Add columns to track ban level, duration, and expiry
ALTER TABLE `phone_blacklist`
  ADD COLUMN `status`     VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' AFTER `operator_id`,
  ADD COLUMN `ban_level`  INT         NOT NULL DEFAULT 1        AFTER `status`,
  ADD COLUMN `ban_days`   INT         DEFAULT NULL              AFTER `ban_level`,
  ADD COLUMN `ban_until`  TIMESTAMP   DEFAULT NULL              AFTER `ban_days`,
  ADD COLUMN `unban_time` TIMESTAMP   DEFAULT NULL              AFTER `ban_until`;

-- 2. Drop the UNIQUE constraint on phone so the same phone can have multiple
--    historical ban records (one row per ban event).
--    Constraint name in MySQL for a single-column unique index == column name.
ALTER TABLE `phone_blacklist` DROP INDEX `phone`;
