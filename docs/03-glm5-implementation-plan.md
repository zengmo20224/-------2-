# GLM5.1 实施计划

日期：2026-06-08

本文档是给 GLM5.1 的执行说明。

## 角色分工

GLM5.1 负责具体 SQL 和代码实现。Codex 负责规划、任务边界、质量门禁和审查标准。

不要跳阶段。某个阶段没有通过退出标准前，不允许把它接入主项目。

## 当前仓库状态

- 阶段 1 数据库 Schema 已完成。
- 阶段 2 后端骨架已完成。
- 阶段 3 核心实体、Mapper 与基础 CRUD Service 已完成。
- 阶段 4 认证与授权已完成。
- 阶段 5 服务预约与排班已形成核心代码提交。
- 阶段 6 社区与内容审核已形成代码提交。
- 阶段 7 商品到店自提订单已形成代码提交。
- 当前准备进入阶段 8 AI Provider 与 AI 功能。
- 每个安全节点都要用 Git 保存。
- 不要先写前端，不要跳过当前阶段门禁。

## 编码前必须做的 Git 操作

先运行：

```powershell
git status --short --branch
git branch
```

然后为当前阶段创建独立分支：

```powershell
git switch -c phase-1-schema
```

如果分支已经存在：

```powershell
git switch phase-1-schema
```

除非用户明确要求，否则不要直接在 `main` 上做阶段实现。

## 阶段 1 强制任务：schema.sql

创建：

```text
schema.sql
```

要求：

- 使用 MySQL 8 兼容 SQL。
- 使用 `CREATE DATABASE IF NOT EXISTS petcare_o2o DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`。
- 每张表使用 InnoDB。
- 主键使用 `BIGINT`。
- 金额使用 `DECIMAL(10,2)`。
- 经纬度使用 `DECIMAL(10,6)`。
- 状态字段使用 `VARCHAR(32)`。
- 每张表添加 `COMMENT`。
- 重要字段添加 `COMMENT`。
- `create_time` 使用 `DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`。
- 有 `update_time` 的表使用 `DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`。
- 有逻辑删除的表使用 `deleted TINYINT NOT NULL DEFAULT 0`。
- 按需求添加唯一索引。
- 为 `user_id`、`store_id`、`staff_id`、`status`、日期、创建时间和关系查询字段添加必要索引。

不允许省略表。

必须包含的表：

- `user`
- `pet`
- `user_address`
- `store`
- `store_config`
- `service_category`
- `service_item`
- `staff`
- `staff_skill`
- `staff_schedule`
- `staff_unavailable_time`
- `staff_booking_lock`
- `service_booking`
- `booking_status_log`
- `topic`
- `post`
- `post_image`
- `post_comment`
- `post_like`
- `post_favorite`
- `post_report`
- `sensitive_word`
- `content_review_record`
- `product_category`
- `product`
- `product_image`
- `cart_item`
- `product_order`
- `product_order_item`
- `marketing_activity`
- `activity_product`
- `activity_service`
- `ai_conversation`
- `ai_message`
- `ai_usage_log`
- `ai_analysis_report`
- `faq_knowledge`
- `admin_user`
- `admin_role`
- `admin_permission`
- `admin_role_permission`
- `admin_operation_log`

其中：

- `staff_booking_lock` 是用户批准预约并发防冲突方案后新增的必需表，设计依据见 `docs/09-booking-concurrency-control.md`。
- `admin_role`、`admin_permission`、`admin_role_permission` 是用户确认细粒度 RBAC 后新增的必需表，设计依据见 `docs/10-admin-permission-design.md`。

## 允许的字段微调

不要随意改变业务设计。

如果确实需要，可在 SQL 注释或 `docs/schema-notes.md` 中说明以下修正：

- 对保留字表名使用反引号，例如 `user`。
- 只在安全的字段上添加明确 `NOT NULL`。
- 即使不声明物理外键，也要给 `store_id` 等逻辑外键字段添加索引。
- `created_by`、`updated_by` 这类审计字段只有用户后续批准时才能添加；阶段 1 不主动添加。

阶段 1 默认不添加物理外键，除非用户明确批准。优先使用“逻辑关系 + 索引”，便于迭代和测试数据构造。

## Schema 验证命令

优先使用 Docker：

```powershell
$env:PETCARE_MYSQL_ROOT_PASSWORD = "replace-with-local-test-password"
docker run --name petcare-mysql -e "MYSQL_ROOT_PASSWORD=$env:PETCARE_MYSQL_ROOT_PASSWORD" -e MYSQL_DATABASE=petcare_o2o -p 3306:3306 -d mysql:8.0
Get-Content -Raw .\schema.sql | docker exec -i petcare-mysql mysql -uroot "-p$env:PETCARE_MYSQL_ROOT_PASSWORD" petcare_o2o
docker exec -i petcare-mysql mysql -uroot "-p$env:PETCARE_MYSQL_ROOT_PASSWORD" -e "USE petcare_o2o; SHOW TABLES;"
```

如果 Docker 不可用，使用本地 MySQL 8：

```powershell
Get-Content -Raw .\schema.sql | mysql -uroot -p
mysql -uroot -p -e "USE petcare_o2o; SHOW TABLES;"
```

抽查：

```powershell
mysql -uroot -p -e "USE petcare_o2o; SHOW CREATE TABLE service_booking;"
mysql -uroot -p -e "USE petcare_o2o; SHOW CREATE TABLE post;"
mysql -uroot -p -e "USE petcare_o2o; SHOW CREATE TABLE admin_user;"
```

## 阶段 1 提交要求

验证通过后：

```powershell
git status --short
git add schema.sql
# 只有可选说明文件存在时才运行：
git add docs/schema-notes.md
git commit -m "feat: add validated mysql schema"
git status --short --branch
```

## 阶段 2 后端规则

只有阶段 1 通过后才能开始。

阶段 2 具体执行说明见 `docs/11-phase-2-backend-skeleton-brief.md`。

后端必须使用：

- Spring Boot 3
- MyBatis-Plus
- Spring Security + JWT
- Java 17 或更高版本
- 默认使用 Maven，除非有充分理由选择 Gradle
- 通过环境变量配置数据库和 AI
- 使用 `docs/01-architecture-design.md` 中的模块包结构
- 使用 MyBatis-Plus 雪花 ID，数据库不使用 `AUTO_INCREMENT`
- 当前仅维护 `schema.sql`，不引入 Flyway 或 Liquibase
- V1 不启用 Redis

## 阶段 3 实体与 Mapper 规则

只有阶段 2 通过后才能开始。

阶段 3 具体执行说明见 `docs/12-phase-3-entities-mappers-plan.md`。

最小包结构：

```text
src/main/java/.../petcare/
  common/
  user/
  store/
  service/
  staff/
  booking/
  community/
  moderation/
  product/
  marketing/
  ai/
  admin/
```

## 阶段 4 认证与授权规则

只有阶段 3 通过后才能开始。

阶段 4 具体执行说明见 `docs/13-phase-4-auth-authorization-plan.md`。

必须实现：

- 管理员账号密码登录。
- BCrypt 密码校验。
- JWT 签发、解析和认证过滤。
- Spring Security 无状态配置。
- 基于 `admin_role`、`admin_permission`、`admin_role_permission` 的权限码加载。
- 使用 `hasAuthority(...)` 进行细粒度授权。
- 微信登录占位接口和 `WechatLoginProvider` 边界。

禁止实现：

- 真实微信 `code2session`。
- Redis Token 黑名单。
- 预约、订单、社区、AI 等后续业务流程。
- 前端工程。

## 阶段 5 服务预约与排班规则

只有阶段 4 通过后才能开始。

阶段 5 具体执行说明见 `docs/14-phase-5-booking-scheduling-plan.md`。

必须实现：

- 服务目录查询。
- 可预约时间计算。
- 用户创建预约的业务服务。
- 系统自动分配可用员工，用户不能指定员工。
- 上门服务距离校验。
- 预约状态流转和状态日志。
- 管理员确认、拒绝、取消、开始、完成、改派。
- `staff_booking_lock` 锁定点事务流程。
- MySQL 8 并发预约冲突测试。

禁止实现：

- 真实微信登录。
- 通过请求体 `userId` 绕过用户认证。
- Redis 分布式锁。
- 微信支付或线上支付。
- 社区、商品订单、AI 和前端工程。

## 阶段 6 社区与内容审核规则

只有阶段 5 通过后才能开始。

阶段 6 具体执行说明见 `docs/15-phase-6-community-moderation-plan.md`。

必须实现：

- 话题查询。
- 帖子发布、列表、详情业务服务。
- 评论发布和列表业务服务。
- 点赞、收藏、举报业务。
- 敏感词匹配。
- 内容审核记录创建。
- 后台帖子、评论、举报和敏感词管理。
- 后台审核权限码校验。

禁止实现：

- 真实文件上传。
- AI 发帖辅助或 AI 内容审核。
- Redis 敏感词缓存。
- 推荐流、热榜算法、关注关系或私信。
- 通过请求体 `userId` 绕过用户认证。

## 阶段 7 商品到店自提订单规则

只有阶段 6 通过后才能开始。

阶段 7 具体执行说明见 `docs/16-phase-7-product-orders-plan.md`。

必须实现：

- 商品分类、商品列表和详情查询。
- 购物车管理和唯一约束防重复。
- 从已选购物车项创建到店自提订单。
- 服务端订单金额计算和订单项价格快照。
- 原子库存扣减、取消恢复库存和并发防超卖。
- 用户订单查询和待确认订单取消。
- 后台确认、备货完成、确认线下收款、完成、取消和缺货取消。
- 后台商品订单权限码校验。

禁止实现：

- 微信支付或其他在线支付。
- 配送、优惠券、积分、会员价和复杂营销计价。
- Redis 分布式锁。
- 商品图片上传。
- 信任客户端价格或订单金额。
- 通过请求体 `userId` 绕过用户认证。

## 阶段 8 AI Provider 与 AI 功能规则

只有阶段 7 通过后才能开始。

阶段 8 具体执行说明见 `docs/17-phase-8-ai-provider-functions-plan.md`。

当前允许立即实现阶段 8A：

- Provider 无关的 `AiProviderClient` 端口。
- 默认关闭的 Provider 实现和 Mock Provider 测试设施。
- AI 会话、消息和用量日志业务。
- AI 客服可信上下文构造和接地规则。
- 宠物高风险症状确定性安全规则。
- 不编造用户事实的发帖辅助。
- 后端经营数据聚合和管理端分析报告。
- 管理端 AI 权限、操作日志和安全错误映射。

当前任务禁止提前实现阶段 8B：

- 在阶段 14B 前实现真实 DeepSeek HTTP Client。
- 偏离 D-008 已决定的模型、超时、重试次数、最大 Token、调用频率和非流式策略。
- 让 AI、Provider Client 或 Prompt 直接访问数据库。
- 让 AI 生成并执行 SQL。
- 让 AI 输出直接改变商品、库存、预约、订单或内容审核状态。
- 把 Provider 原始错误、完整 Prompt、API Key 或 Authorization Header 写入响应或日志。
- 通过请求体 `userId` 或 `adminId` 绕过认证。

## 业务逻辑 TDD 规则

每个非平凡模块都要按以下流程做：

1. 先写测试。
2. 运行测试并确认出现预期的 RED 失败。
3. 实现最小代码。
4. 再次运行测试并确认 GREEN。
5. 只在测试保持绿色时重构。
6. 提交当前阶段。

早期必须覆盖的测试：

- 距离计算
- 时间段重叠判断
- 可预约时间槽生成
- 预约状态流转
- 敏感词风险分级
- 订单金额计算
- AI Provider 缺少 API Key 时的行为
- AI Provider 数据库隔离
- AI 客服上下文接地
- 宠物高风险症状安全拒答
- AI 发帖事实约束
- 并发预约只允许一个请求成功

## 集成规则

一个阶段只有满足以下条件后才算接入完成：

- 相关测试通过。
- 构建通过。
- 如果配置了 lint 或格式化检查，也必须通过。
- 安全检查通过。
- 存在 Git 提交。
- 满足 `docs/07-integration-gates.md` 对应清单。

## 阶段交接报告格式

每个阶段结束时按以下格式汇报：

```text
阶段：
分支：
提交：
变更文件：
验证命令：
验证结果：
已知限制：
下一阶段建议：
```
