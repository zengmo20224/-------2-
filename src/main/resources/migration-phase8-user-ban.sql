-- Phase 8: User ban feature — phone blacklist table + user management permissions
-- Run against dev MySQL. Idempotent via IF NOT EXISTS / ON DUPLICATE KEY UPDATE.

-- 1. Phone blacklist table
CREATE TABLE IF NOT EXISTS `phone_blacklist` (
  `id`           BIGINT       NOT NULL,
  `phone`        VARCHAR(20)  NOT NULL,
  `user_id`      BIGINT       DEFAULT NULL,
  `reason`       VARCHAR(255) DEFAULT NULL,
  `operator_id`  BIGINT       DEFAULT NULL,
  `create_time`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE (`phone`)
);

-- 2. New permission codes for user management (id 7043-7044)
INSERT INTO `admin_permission` (`id`, `permission_code`, `permission_name`, `module`, `status`) VALUES
  (7043, 'user:profile:read', '用户查看', 'user', 'ACTIVE'),
  (7044, 'user:profile:ban',  '用户封禁/解封', 'user', 'ACTIVE')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 'ACTIVE';

-- 3. Grant new permissions to SUPER_ADMIN (role 1) and ADMIN (role 2)
INSERT INTO `admin_role_permission` (`id`, `role_id`, `permission_id`) VALUES
  (80043, 1, 7043), (80044, 1, 7044),
  (81043, 2, 7043), (81044, 2, 7044)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);
