-- ============================================================================
-- Phase 5: Notification + Announcement tables
-- ============================================================================

-- Official announcements (published by admin)
CREATE TABLE IF NOT EXISTS `announcement` (
  `id`           BIGINT        NOT NULL COMMENT '主键，雪花 ID',
  `title`        VARCHAR(200)  NOT NULL COMMENT '公告标题',
  `content`      TEXT          NOT NULL COMMENT '公告正文（支持简单 HTML）',
  `status`       VARCHAR(32)   NOT NULL DEFAULT 'PUBLISHED' COMMENT 'DRAFT / PUBLISHED',
  `sort`         INT           NOT NULL DEFAULT 0 COMMENT '排序权重（越小越靠前）',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='官方公告表';

-- User-facing notification records (like, comment, favorite on user's posts)
CREATE TABLE IF NOT EXISTS `user_notification` (
  `id`             BIGINT        NOT NULL COMMENT '主键，雪花 ID',
  `user_id`        BIGINT        NOT NULL COMMENT '接收者用户 ID（帖子作者）',
  `actor_id`       BIGINT        DEFAULT NULL COMMENT '触发者用户 ID',
  `type`           VARCHAR(32)   NOT NULL COMMENT 'LIKE / COMMENT / FAVORITE',
  `post_id`        BIGINT        DEFAULT NULL COMMENT '关联帖子 ID',
  `comment_id`     BIGINT        DEFAULT NULL COMMENT '关联评论 ID（评论通知时）',
  `content`        VARCHAR(500)  DEFAULT NULL COMMENT '通知摘要（如评论内容前50字）',
  `is_read`        TINYINT       NOT NULL DEFAULT 0 COMMENT '0-未读 1-已读',
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_user_create` (`user_id`, `create_time`),
  KEY `idx_post_type` (`post_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知表';

-- ============================================================================
-- Seed data: sample announcements
-- ============================================================================
INSERT INTO `announcement` (`id`, `title`, `content`, `status`, `sort`) VALUES
(1001, '欢迎来到 PetCare 社区', 'PetCare 宠物社区正式上线！在这里你可以分享和宠物的日常故事，与其他宠主交流养宠心得，还可以预约专业的宠物服务。', 'PUBLISHED', 0),
(1002, '夏季宠物护理小贴士', '夏天到了，请注意给宠物补充水分，避免长时间暴晒。我们提供专业的洗护和美容服务，让你的宠物清凉一夏！', 'PUBLISHED', 1),
(1003, '社区发帖指南', '欢迎大家积极发帖分享。请遵守社区规范，文明交流。支持上传最多9张图片，添加标签让更多人看到你的内容。', 'PUBLISHED', 2);

