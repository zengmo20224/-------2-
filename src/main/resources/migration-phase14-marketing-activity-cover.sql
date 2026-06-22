-- Phase 14: add marketing activity cover image support.
-- Existing rows remain valid; cover_url is optional.

ALTER TABLE `marketing_activity`
  ADD COLUMN `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '活动封面图 URL' AFTER `description`;
