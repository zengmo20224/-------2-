-- ============================================================================
-- Phase 2: Comment enhancement — comment likes table
-- Run after schema.sql and migration-phase1-tags.sql
-- ============================================================================

USE petcare_o2o;

-- 评论点赞记录表（幂等：unique key 防止重复点赞）
CREATE TABLE IF NOT EXISTS `comment_like` (
  `id`          BIGINT   NOT NULL COMMENT '主键，雪花 ID',
  `comment_id`  BIGINT   NOT NULL COMMENT '评论 ID',
  `user_id`     BIGINT   NOT NULL COMMENT '点赞用户 ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞记录表';
