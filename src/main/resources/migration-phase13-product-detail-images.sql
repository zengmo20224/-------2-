-- Phase 13: Separate product intro images from product detail carousel images.

CREATE TABLE IF NOT EXISTS `product_detail_image` (
  `id`          BIGINT       NOT NULL,
  `product_id`  BIGINT       NOT NULL,
  `image_url`   VARCHAR(255) NOT NULL,
  `sort`        INT          NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品介绍图片表';
