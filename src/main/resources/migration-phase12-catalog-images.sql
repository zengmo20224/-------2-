-- Phase 12: Catalog detail galleries and product page carousel.

CREATE TABLE IF NOT EXISTS `service_item_image` (
  `id`              BIGINT       NOT NULL,
  `service_item_id` BIGINT       NOT NULL,
  `image_url`       VARCHAR(255) NOT NULL,
  `sort`            INT          NOT NULL DEFAULT 0,
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_service_item_id` (`service_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目图片表';

CREATE TABLE IF NOT EXISTS `product_carousel_image` (
  `id`             BIGINT       NOT NULL,
  `title`          VARCHAR(80)  DEFAULT NULL,
  `image_url`      VARCHAR(255) NOT NULL,
  `link_type`      VARCHAR(32)  DEFAULT 'NONE',
  `link_target_id` BIGINT       DEFAULT NULL,
  `status`         VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
  `sort`           INT          NOT NULL DEFAULT 0,
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品展示页轮播图表';
