-- ============================================================================
-- Phase 1: Community tag system migration
-- Run after schema.sql on existing database
-- ============================================================================

USE petcare_o2o;

-- 标签表（用户自定义标签，通过 # 前缀使用）
CREATE TABLE IF NOT EXISTS `community_tag` (
  `id`          BIGINT      NOT NULL COMMENT '主键，雪花 ID',
  `name`        VARCHAR(64) NOT NULL COMMENT '标签名（不含#）',
  `usage_count` INT         NOT NULL DEFAULT 0 COMMENT '被使用次数',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_usage_count` (`usage_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区标签表';

-- 帖子-标签关联表（多对多）
CREATE TABLE IF NOT EXISTS `post_tag_rel` (
  `id`          BIGINT   NOT NULL COMMENT '主键，雪花 ID',
  `post_id`     BIGINT   NOT NULL COMMENT '帖子 ID',
  `tag_id`      BIGINT   NOT NULL COMMENT '标签 ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';
