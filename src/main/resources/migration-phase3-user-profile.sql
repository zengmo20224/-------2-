-- ============================================================================
-- Phase 3: User profile enhancement — real name, ID card, avatar upload
-- ============================================================================

USE petcare_o2o;

ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名' AFTER `nickname`,
  ADD COLUMN IF NOT EXISTS `id_card_no` VARCHAR(18) DEFAULT NULL COMMENT '身份证号' AFTER `real_name`,
  ADD COLUMN IF NOT EXISTS `id_card_image_url` VARCHAR(255) DEFAULT NULL COMMENT '身份证照片URL' AFTER `id_card_no`;
